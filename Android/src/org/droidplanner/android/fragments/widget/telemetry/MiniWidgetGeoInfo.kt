package org.droidplanner.android.fragments.widget.telemetry

import android.content.*
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent
import com.o3dr.services.android.lib.drone.attribute.AttributeType
import com.o3dr.services.android.lib.drone.property.Gps
import org.droidplanner.android.R
import org.droidplanner.android.fragments.widget.TowerWidget
import org.droidplanner.android.fragments.widget.TowerWidgets

/**
 * Created by Fredia Huya-Kouadio on 9/20/15.
 */
class MiniWidgetGeoInfo : TowerWidget() {

    companion object {
        private val filter = initFilter()

        private fun initFilter(): IntentFilter {
            val temp = IntentFilter()
            temp.addAction(AttributeEvent.GPS_POSITION)
            temp.addAction(AttributeEvent.HOME_UPDATED)
            return temp
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                AttributeEvent.GPS_POSITION, AttributeEvent.HOME_UPDATED -> onPositionUpdate()
            }
        }
    }

    private var latitude: TextView? = null
    private var longitude: TextView? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_mini_widget_geo_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        latitude = view.findViewById(R.id.latitude_telem) as TextView?
        longitude = view.findViewById(R.id.longitude_telem) as TextView?

        val clipboardMgr = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val container = view.findViewById(R.id.mini_widget_geo_info_layout)
        container?.setOnClickListener {
            val drone = drone
            if(drone.isConnected) {
                val droneGps = drone.getAttribute<Gps>(AttributeType.GPS)
                if(droneGps.isValid) {
                    //Copy the lat long to the clipboard.
                    val latLongText = "${droneGps.position.latitude}, ${droneGps.position.longitude}"
                    val clipData = ClipData.newPlainText("Vehicle Lat/Long", latLongText)

                    clipboardMgr.primaryClip = clipData

                    Toast.makeText(context, "Copied lat/long data", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getWidgetType() = TowerWidgets.GEO_INFO

    override fun onApiConnected() {
        onPositionUpdate()
        broadcastManager.registerReceiver(receiver, filter)
    }

    override fun onApiDisconnected() {
        broadcastManager.unregisterReceiver(receiver)
    }

    private fun onPositionUpdate() {
        if (!isAdded)
            return

        val drone = drone
        val droneGps = drone.getAttribute<Gps>(AttributeType.GPS) ?: return

        if (droneGps.isValid) {

            val latitudeValue = droneGps.position.latitude
            val longitudeValue = droneGps.position.longitude

            latitude?.text = getString(R.string.latitude_telem, Location.convert(latitudeValue, Location.FORMAT_DEGREES).toString())
            longitude?.text = getString(R.string.longitude_telem, Location.convert(longitudeValue, Location.FORMAT_DEGREES).toString())

        }
    }
}