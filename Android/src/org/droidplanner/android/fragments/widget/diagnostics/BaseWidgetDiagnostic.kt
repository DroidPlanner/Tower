package org.droidplanner.android.fragments.widget.diagnostics

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent
import com.o3dr.services.android.lib.drone.attribute.AttributeType
import com.o3dr.services.android.lib.drone.property.EkfStatus
import com.o3dr.services.android.lib.drone.property.State
import com.o3dr.services.android.lib.drone.property.Vibration
import org.droidplanner.android.fragments.widget.TowerWidget
import org.droidplanner.android.fragments.widget.TowerWidgets

/**
 * Created by Fredia Huya-Kouadio on 8/30/15.
 */
public abstract class BaseWidgetDiagnostic : TowerWidget(){

    companion object {
        private val filter = initFilter()

        private fun initFilter(): IntentFilter {
            val temp = IntentFilter()
            temp.addAction(AttributeEvent.STATE_EKF_REPORT)

            temp.addAction(AttributeEvent.STATE_CONNECTED)
            temp.addAction(AttributeEvent.STATE_DISCONNECTED)
            temp.addAction(AttributeEvent.HEARTBEAT_RESTORED)
            temp.addAction(AttributeEvent.HEARTBEAT_TIMEOUT)

            temp.addAction(AttributeEvent.STATE_VEHICLE_VIBRATION)
            return temp
        }

        val INVALID_HIGHEST_VARIANCE: Float = -1f

        /**
         * Any variance value less than this threshold is considered good.
         */
        val GOOD_VARIANCE_THRESHOLD: Float = 0.5f

        /**
         * Variance values between the good threshold and the warning threshold are considered as warning.
         * Variance values above the warning variance threshold are considered bad.
         */
        val WARNING_VARIANCE_THRESHOLD: Float = 0.8f

        /**
         * Vibration values less or equal to this value are considered good.
         */
        val GOOD_VIBRATION_THRESHOLD: Int = 30

        /**
         * Vibration values between the good threshold and the warning threshold are in the warning zone.
         * Vibration values above the warning threshold are in the danger zone.
         */
        val WARNING_VIBRATION_THRESHOLD: Int = 60
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                AttributeEvent.STATE_EKF_REPORT -> updateEkfStatus()

                AttributeEvent.STATE_CONNECTED,
                AttributeEvent.STATE_DISCONNECTED,
                AttributeEvent.HEARTBEAT_RESTORED,
                AttributeEvent.HEARTBEAT_TIMEOUT -> {
                    updateEkfStatus()
                    updateVibrationStatus()
                }

                AttributeEvent.STATE_VEHICLE_VIBRATION -> updateVibrationStatus()
            }
        }
    }

    private fun updateEkfStatus(){
        if (!isAdded)
            return

        val state: State? = drone?.getAttribute(AttributeType.STATE)
        val ekfStatus = state?.ekfStatus
        if (state == null || !state.isTelemetryLive || ekfStatus == null) {
            disableEkfView()
        }
        else{
            updateEkfView(ekfStatus)
        }
    }

    private fun updateVibrationStatus(){
        if(!isAdded)
            return

        val state: State? = drone?.getAttribute(AttributeType.STATE)
        val vibration = state?.vehicleVibration
        if(state == null || !state.isTelemetryLive || vibration == null){
            disableVibrationView()
        }
        else{
            updateVibrationView(vibration)
        }
    }

    protected open fun disableEkfView(){}

    protected open fun updateEkfView(ekfStatus: EkfStatus){}

    protected open fun disableVibrationView(){}

    protected open fun updateVibrationView(vibration: Vibration){}

    override fun getWidgetType() = TowerWidgets.VEHICLE_DIAGNOSTICS

    override fun onApiConnected() {
        updateEkfStatus()
        updateVibrationStatus()
        broadcastManager.registerReceiver(receiver, filter)
    }

    override fun onApiDisconnected() {
        broadcastManager.unregisterReceiver(receiver)
        updateEkfStatus()
        updateVibrationStatus()
    }
}