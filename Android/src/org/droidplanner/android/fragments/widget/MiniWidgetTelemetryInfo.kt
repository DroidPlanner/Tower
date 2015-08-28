package org.droidplanner.android.fragments.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent
import com.o3dr.services.android.lib.drone.attribute.AttributeType
import com.o3dr.services.android.lib.drone.property.Attitude
import com.o3dr.services.android.lib.drone.property.Speed
import org.droidplanner.android.R
import org.droidplanner.android.fragments.helpers.ApiListenerFragment
import org.droidplanner.android.view.AttitudeIndicator
import java.util.*

/**
 * Created by Fredia Huya-Kouadio on 8/27/15.
 */
public class MiniWidgetTelemetryInfo : TowerWidget() {

    companion object {
        private val filter = initFilter()

        private fun initFilter(): IntentFilter {
            val temp = IntentFilter()
            temp.addAction(AttributeEvent.ATTITUDE_UPDATED)
            temp.addAction(AttributeEvent.SPEED_UPDATED)
            return temp
        }
    }

    private val receiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.getAction()){
                AttributeEvent.ATTITUDE_UPDATED -> onOrientationUpdate()
                AttributeEvent.SPEED_UPDATED -> onSpeedUpdate()
            }
        }

    }

    private var attitudeIndicator: AttitudeIndicator? = null
    private var roll: TextView? = null
    private var yaw: TextView? = null
    private var pitch: TextView? = null

    private var horizontalSpeed: TextView? = null
    private var verticalSpeed: TextView? = null

    private var headingModeFPV: Boolean = false

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        return inflater?.inflate(R.layout.fragment_mini_widget_telemetry_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        attitudeIndicator = view.findViewById(R.id.aiView) as AttitudeIndicator

        roll = view.findViewById(R.id.rollValueText) as TextView
        yaw = view.findViewById(R.id.yawValueText) as TextView
        pitch = view.findViewById(R.id.pitchValueText) as TextView

        horizontalSpeed = view.findViewById(R.id.horizontal_speed_telem) as TextView
        verticalSpeed = view.findViewById(R.id.vertical_speed_telem) as TextView
    }

    override fun onStart() {
        super.onStart()

        val prefs = PreferenceManager.getDefaultSharedPreferences(getContext())
        headingModeFPV = prefs.getBoolean("pref_heading_mode", false)
    }

    override fun getWidgetId() = R.id.tower_widget_telemetry_info

    override fun onApiConnected() {
        updateAllTelem()
        getBroadcastManager().registerReceiver(receiver, filter)
    }

    override fun onApiDisconnected() {
        getBroadcastManager().unregisterReceiver(receiver)
    }

    private fun updateAllTelem() {
        onOrientationUpdate()
        onSpeedUpdate()
    }

    private fun onOrientationUpdate() {
        val drone = getDrone()

        val attitude = drone.getAttribute<Attitude>(AttributeType.ATTITUDE) ?: return

        val r = attitude.getRoll().toFloat()
        val p = attitude.getPitch().toFloat()
        var y = attitude.getYaw().toFloat()

        if (!headingModeFPV and (y < 0)) {
            y += 360
        }

        attitudeIndicator?.setAttitude(r, p, y)

        roll?.setText(java.lang.String.format(Locale.US,"%3.0f\u00B0", r))
        pitch?.setText(java.lang.String.format(Locale.US,"%3.0f\u00B0", p))
        yaw?.setText(java.lang.String.format(Locale.US, "%3.0f\u00B0", y))

    }

    private fun onSpeedUpdate() {
        val drone = getDrone()
        val speed = drone.getAttribute<Speed>(AttributeType.SPEED) ?: return

        val groundSpeedValue =  speed.getGroundSpeed()
        val verticalSpeedValue = speed.getVerticalSpeed()

        val speedUnitProvider = getSpeedUnitProvider()

        horizontalSpeed?.setText(getString(R.string.horizontal_speed_telem, speedUnitProvider.boxBaseValueToTarget(groundSpeedValue).toString()))
        verticalSpeed?.setText(getString(R.string.vertical_speed_telem, speedUnitProvider.boxBaseValueToTarget(verticalSpeedValue).toString()))
    }
}