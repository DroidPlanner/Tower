package org.droidplanner.android.fragments.widget.diagnostics

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.o3dr.services.android.lib.drone.property.EkfStatus
import com.o3dr.services.android.lib.drone.property.Vibration
import org.droidplanner.android.R
import kotlin.properties.Delegates

/**
 * Created by Fredia Huya-Kouadio on 8/29/15.
 */
public class MiniWidgetDiagnostics : BaseWidgetDiagnostic() {

    private val okFlagDrawable: Drawable? by lazy(LazyThreadSafetyMode.NONE) {
        resources?.getDrawable(R.drawable.ic_check_box_green_500_18dp)
    }

    private val badFlagDrawable: Drawable? by lazy(LazyThreadSafetyMode.NONE) {
        resources?.getDrawable(R.drawable.ic_cancel_red_500_18dp)
    }

    private val warningFlagDrawable by lazy(LazyThreadSafetyMode.NONE) {
        resources?.getDrawable(R.drawable.ic_warning_orange_500_18dp)
    }

    private val unknownFlagDrawable: Drawable? by lazy(LazyThreadSafetyMode.NONE) {
        resources?.getDrawable(R.drawable.ic_help_grey_600_18dp)
    }

    private val ekfStatusView by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById(R.id.ekf_status) as TextView?
    }

    private val vibrationStatus by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById(R.id.vibration_status) as TextView?
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_mini_widget_ekf_status, container, false)
    }

    override fun updateEkfView(ekfStatus: EkfStatus) {
        val ekfVar = Math.max(ekfStatus.velocityVariance,
                Math.max(ekfStatus.horizontalPositionVariance,
                        Math.max(ekfStatus.verticalPositionVariance,
                                Math.max(ekfStatus.compassVariance, ekfStatus.terrainAltitudeVariance))))

        val statusDrawable = if (ekfVar < BaseWidgetDiagnostic.GOOD_VARIANCE_THRESHOLD) okFlagDrawable
        else if (ekfVar < BaseWidgetDiagnostic.WARNING_VARIANCE_THRESHOLD) warningFlagDrawable
        else badFlagDrawable

        ekfStatusView?.setCompoundDrawablesWithIntrinsicBounds(null, statusDrawable, null, null)
    }

    override fun disableEkfView() {
        ekfStatusView?.setCompoundDrawablesWithIntrinsicBounds(null, unknownFlagDrawable, null, null)
    }

    override fun updateVibrationView(vibration: Vibration){
        val vibSummary = Math.max(vibration.vibrationX,
                Math.max(vibration.vibrationY, vibration.vibrationZ))

        val statusDrawable = if(vibSummary < BaseWidgetDiagnostic.GOOD_VIBRATION_THRESHOLD) okFlagDrawable
        else if(vibSummary < BaseWidgetDiagnostic.WARNING_VIBRATION_THRESHOLD) warningFlagDrawable
        else badFlagDrawable

        vibrationStatus?.setCompoundDrawablesWithIntrinsicBounds(null, statusDrawable, null, null)
    }

    override fun disableVibrationView(){
        vibrationStatus?.setCompoundDrawablesWithIntrinsicBounds(null, unknownFlagDrawable, null, null)
    }
}