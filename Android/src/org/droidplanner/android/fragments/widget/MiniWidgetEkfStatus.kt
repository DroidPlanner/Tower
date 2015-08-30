package org.droidplanner.android.fragments.widget

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.o3dr.services.android.lib.drone.property.EkfStatus
import org.droidplanner.android.R

/**
 * Created by Fredia Huya-Kouadio on 8/29/15.
 */
public class MiniWidgetEkfStatus : BaseWidgetEkfStatus() {

    private var goodStatus: Drawable? = null
    private var warningStatus: Drawable? = null
    private var dangerStatus: Drawable? = null
    private var disabledStatus: Drawable? = null

    private var ekfLabel: TextView? = null
    private var ekfHighestVar: Float = BaseWidgetEkfStatus.INVALID_HIGHEST_VARIANCE

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

    override fun updateEkfView(ekfStatus: EkfStatus) {
        val res = getResources()
        updateVarianceView(velocityVar, ekfStatus.getVelocityVariance())
        updateVarianceView(horizontalPosVar, ekfStatus.getHorizontalPositionVariance())
        updateVarianceView(verticalPosVar, ekfStatus.getVerticalPositionVariance())
        updateVarianceView(magVar, ekfStatus.getCompassVariance())
        updateVarianceView(terrainVar, ekfStatus.getTerrainAltitudeVariance())

        val textColor = if (ekfHighestVar < BaseWidgetEkfStatus.GOOD_VARIANCE_THRESHOLD) android.R.color.holo_green_dark
        else if (ekfHighestVar < BaseWidgetEkfStatus.WARNING_VARIANCE_THRESHOLD) android.R.color.holo_orange_dark
        else android.R.color.holo_red_dark

        ekfLabel?.setTextColor(res.getColor(textColor))
    }

    override fun disableEkfView() {
        val res = getResources()
        ekfLabel?.setTextColor(res.getColor(R.color.greyText))
        disableVarianceView(velocityVar)
        disableVarianceView(horizontalPosVar)
        disableVarianceView(verticalPosVar)
        disableVarianceView(magVar)
        disableVarianceView(terrainVar)
    }

    protected fun disableVarianceView(varianceView: TextView?) {
        varianceView?.setCompoundDrawablesWithIntrinsicBounds(null, disabledStatus, null, null)
    }

    protected fun updateVarianceView(varianceView: TextView?, variance: Float) {
        ekfHighestVar = Math.max(ekfHighestVar, variance)

        val statusDrawable = if (variance < BaseWidgetEkfStatus.GOOD_VARIANCE_THRESHOLD) goodStatus
        else if (variance < BaseWidgetEkfStatus.WARNING_VARIANCE_THRESHOLD) warningStatus
        else dangerStatus

        varianceView?.setCompoundDrawablesWithIntrinsicBounds(null, statusDrawable, null, null)
    }
}