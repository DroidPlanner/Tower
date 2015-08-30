package org.droidplanner.android.fragments.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent
import com.o3dr.services.android.lib.drone.attribute.AttributeType
import com.o3dr.services.android.lib.drone.property.State
import org.droidplanner.android.R

/**
 * Created by Fredia Huya-Kouadio on 8/29/15.
 */
public class MiniWidgetEkfStatus : TowerWidget() {

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

        private val INVALID_HIGHEST_VARIANCE = -1f

        /**
         * Any variance value less than this threshold is considered good.
         */
        private val GOOD_VARIANCE_THRESHOLD = 0.5f

        /**
         * Variance values between the good threshold and the warning threshold are considered as warning.
         * Variance values above the warning variance threshold are considered bad.
         */
        private val WARNING_VARIANCE_THRESHOLD = 0.8f
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

    private var goodStatus: Drawable? = null
    private var warningStatus: Drawable? = null
    private var dangerStatus: Drawable? = null
    private var disabledStatus: Drawable? = null

    private var ekfLabel: TextView? = null
    private var ekfHighestVar: Float = INVALID_HIGHEST_VARIANCE

    private var velocityVar: TextView? = null
    private var horizontalPosVar: TextView? = null
    private var verticalPosVar: TextView? = null
    private var magVar: TextView? = null
    private var terrainVar: TextView? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_mini_widget_ekf_status, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ekfLabel = view.findViewById(R.id.ekf_label) as TextView?
        velocityVar = view.findViewById(R.id.velocity_var_status) as TextView?
        horizontalPosVar = view.findViewById(R.id.horizontal_position_var_status) as TextView?
        verticalPosVar = view.findViewById(R.id.vertical_position_var_status) as TextView?
        magVar = view.findViewById(R.id.mag_var_status) as TextView?
        terrainVar = view.findViewById(R.id.terrain_var_status) as TextView?

        val res = getResources()
        goodStatus = res.getDrawable(R.drawable.green_circle_10dp)
        warningStatus = res.getDrawable(R.drawable.orange_circle_10dp)
        dangerStatus = res.getDrawable(R.drawable.red_circle_10dp)
        disabledStatus = res.getDrawable(R.drawable.grey_circle_10dp)
    }

    override fun onApiConnected() {
        updateEkfStatus()
        getBroadcastManager().registerReceiver(receiver, filter)
    }

    override fun onApiDisconnected() {
        getBroadcastManager().unregisterReceiver(receiver)
        updateEkfStatus()
    }

    override fun getWidgetId() = R.id.tower_widget_ekf_status

    fun updateEkfStatus() {
        if (isDetached())
            return

        val res = getResources()
        val state: State? = getDrone()?.getAttribute(AttributeType.STATE)
        val ekfStatus = state?.getEkfStatus()
        if (state == null || !state.isTelemetryLive() || ekfStatus == null) {
            ekfLabel?.setTextColor(res.getColor(R.color.greyText))
            disableVarianceView(velocityVar)
            disableVarianceView(horizontalPosVar)
            disableVarianceView(verticalPosVar)
            disableVarianceView(magVar)
            disableVarianceView(terrainVar)
        } else {
            updateVarianceView(velocityVar, ekfStatus.getVelocityVariance())
            updateVarianceView(horizontalPosVar, ekfStatus.getHorizontalPositionVariance())
            updateVarianceView(verticalPosVar, ekfStatus.getVerticalPositionVariance())
            updateVarianceView(magVar, ekfStatus.getCompassVariance())
            updateVarianceView(terrainVar, ekfStatus.getTerrainAltitudeVariance())

            val textColor = if (ekfHighestVar < GOOD_VARIANCE_THRESHOLD) android.R.color.holo_green_dark
            else if (ekfHighestVar < WARNING_VARIANCE_THRESHOLD) android.R.color.holo_orange_dark
            else android.R.color.holo_red_dark

            ekfLabel?.setTextColor(res.getColor(textColor))
        }
    }

    protected fun disableVarianceView(varianceView: TextView?) {
        varianceView?.setCompoundDrawablesWithIntrinsicBounds(null, disabledStatus, null, null)
    }

    protected fun updateVarianceView(varianceView: TextView?, variance: Float) {
        ekfHighestVar = Math.max(ekfHighestVar, variance)

        val statusDrawable = if (variance < GOOD_VARIANCE_THRESHOLD) goodStatus
        else if (variance < WARNING_VARIANCE_THRESHOLD) warningStatus
        else dangerStatus

        varianceView?.setCompoundDrawablesWithIntrinsicBounds(null, statusDrawable, null, null)
    }
}