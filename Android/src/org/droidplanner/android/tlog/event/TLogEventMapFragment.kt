package org.droidplanner.android.tlog.event

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.widget.Toast
import com.MAVLink.common.msg_global_position_int
import com.o3dr.android.client.utils.data.tlog.TLogParser
import com.o3dr.services.android.lib.coordinate.LatLong
import org.droidplanner.android.R
import org.droidplanner.android.fragments.DroneMap
import org.droidplanner.android.maps.MarkerInfo
import org.droidplanner.android.maps.PolylineInfo
import org.droidplanner.android.tlog.interfaces.TLogDataSubscriber
import org.droidplanner.android.utils.prefs.AutoPanMode
import java.util.*

/**
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
class TLogEventMapFragment : DroneMap(), TLogDataSubscriber, TLogEventClickListener {

    private val eventsPolylineInfo = TLogEventsPolylineInfo()
    private val selectedPositionMarkerInfo = GlobalPositionMarkerInfo()

    override fun isMissionDraggable() = false

    override fun setAutoPanMode(target: AutoPanMode?): Boolean {
        return when(target){
            AutoPanMode.DISABLED -> true
            else -> {
                Toast.makeText(activity, "Auto pan is not supported on this map.",
                        Toast.LENGTH_LONG).show()
                false
            }
        }
    }

    override fun onTLogDataLoaded(events: List<TLogParser.Event>) {
        eventsPolylineInfo.clear()
        for(event in events){
            val globalPositionInt = event.mavLinkMessage as msg_global_position_int
            eventsPolylineInfo.addCoord(
                    LatLong(globalPositionInt.lat.toDouble()/ 1E7, globalPositionInt.lon.toDouble()/ 1E7))
        }
        eventsPolylineInfo.update(this)
    }

    override fun onTLogEventClick(event: TLogParser.Event) {
        //Add a marker for the selected event
        selectedPositionMarkerInfo.selectedGlobalPosition = event.mavLinkMessage as msg_global_position_int
        selectedPositionMarkerInfo.update(this)
    }

    private class TLogEventsPolylineInfo : PolylineInfo() {

        private val eventCoords = ArrayList<LatLong>()

        fun clear(refreshPolyline: Boolean = false) {
            eventCoords.clear()
            if(refreshPolyline) {
                updatePolyline()
            }
        }

        fun addCoord(coord: LatLong){
            eventCoords.add(coord)
        }

        fun update(mapHandle: TLogEventMapFragment) {
            if(isOnMap()) {
                updatePolyline()
            }
            else {
                if(eventCoords.isNotEmpty())
                    mapHandle.addPolyline(this)
            }
        }

        override fun getPoints(): List<LatLong> {
            return eventCoords
        }

        override fun getColor() = 0xfffd693f.toInt()

    }

    private class GlobalPositionMarkerInfo : MarkerInfo() {
        var selectedGlobalPosition : msg_global_position_int? = null

        fun update(mapHandle: TLogEventMapFragment){
            if(isOnMap)
                updateMarker(mapHandle.resources)
            else
                mapHandle.addMarker(this)
        }

        override fun isVisible() = selectedGlobalPosition != null

        override fun getPosition() =
                if(selectedGlobalPosition == null) null
                else LatLong(selectedGlobalPosition!!.lat.toDouble() / 1E7, selectedGlobalPosition!!.lon.toDouble() / 1E7)

        override fun getIcon(res: Resources) = BitmapFactory.decodeResource(res, R.drawable.ic_wp_map_selected)
    }
}