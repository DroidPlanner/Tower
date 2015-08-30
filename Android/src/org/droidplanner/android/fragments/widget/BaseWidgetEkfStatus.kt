package org.droidplanner.android.fragments.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent
import com.o3dr.services.android.lib.drone.attribute.AttributeType
import com.o3dr.services.android.lib.drone.property.EkfStatus
import com.o3dr.services.android.lib.drone.property.State
import org.droidplanner.android.R

/**
 * Created by Fredia Huya-Kouadio on 8/30/15.
 */
public abstract class BaseWidgetEkfStatus : TowerWidget(){

    companion object {
        private val filter = initFilter()

        private fun initFilter(): IntentFilter {
            val temp = IntentFilter()
            temp.addAction(AttributeEvent.STATE_EKF_REPORT)
            temp.addAction(AttributeEvent.STATE_CONNECTED)
            temp.addAction(AttributeEvent.STATE_DISCONNECTED)
            temp.addAction(AttributeEvent.HEARTBEAT_RESTORED)
            temp.addAction(AttributeEvent.HEARTBEAT_TIMEOUT)
            return temp
        }

        protected val INVALID_HIGHEST_VARIANCE: Float = -1f

        /**
         * Any variance value less than this threshold is considered good.
         */
        protected val GOOD_VARIANCE_THRESHOLD: Float = 0.5f

        /**
         * Variance values between the good threshold and the warning threshold are considered as warning.
         * Variance values above the warning variance threshold are considered bad.
         */
        protected val WARNING_VARIANCE_THRESHOLD: Float = 0.8f
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getAction()) {
                AttributeEvent.STATE_EKF_REPORT -> updateEkfStatus()
                AttributeEvent.STATE_CONNECTED -> updateEkfStatus()
                AttributeEvent.STATE_DISCONNECTED -> updateEkfStatus()
                AttributeEvent.HEARTBEAT_RESTORED -> updateEkfStatus()
                AttributeEvent.HEARTBEAT_TIMEOUT -> updateEkfStatus()
            }
        }
    }

    private fun updateEkfStatus(){
        if (isDetached())
            return

        val state: State? = getDrone()?.getAttribute(AttributeType.STATE)
        val ekfStatus = state?.getEkfStatus()
        if (state == null || !state.isTelemetryLive() || ekfStatus == null) {
            disableEkfView()
        }
        else{
            updateEkfView(ekfStatus)
        }
    }

    protected abstract fun disableEkfView()

    protected abstract fun updateEkfView(ekfStatus: EkfStatus)

    override fun getWidgetType() = TowerWidgets.EKF_STATUS

    override fun onApiConnected() {
        updateEkfStatus()
        getBroadcastManager().registerReceiver(receiver, filter)
    }

    override fun onApiDisconnected() {
        getBroadcastManager().unregisterReceiver(receiver)
        updateEkfStatus()
    }
}