package org.droidplanner.android.fragments.widget.diagnostics

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.annotation.StringRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.o3dr.services.android.lib.drone.property.EkfStatus
import org.droidplanner.android.R
import org.droidplanner.android.fragments.widget.diagnostics.BaseWidgetDiagnostic
import java.util.*
import kotlin.properties.Delegates

/**
 * Created by Fredia Huya-Kouadio on 9/15/15.
 */
public class EkfFlagsViewer : BaseWidgetDiagnostic() {

    companion object {
        @StringRes public val LABEL_ID: Int = R.string.title_ekf_flags_viewer
    }

    private val ekfFlagsViews: HashMap<EkfStatus.EkfFlags, TextView?> = HashMap()

    private val okFlagDrawable: Drawable? by lazy(LazyThreadSafetyMode.NONE) {
        resources?.getDrawable(R.drawable.ic_check_box_green_500_24dp)
    }

    private val badFlagDrawable: Drawable? by lazy(LazyThreadSafetyMode.NONE) {
        resources?.getDrawable(R.drawable.ic_cancel_red_500_24dp)
    }

    private val unknownFlagDrawable: Drawable? by lazy(LazyThreadSafetyMode.NONE) {
        resources?.getDrawable(R.drawable.ic_help_orange_500_24dp)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_ekf_flags_viewer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        setupEkfFlags(view)
    }

    override fun disableEkfView() {
        disableEkfFlags()
    }

    override fun updateEkfView(ekfStatus: EkfStatus) {
        updateEkfFlags(ekfStatus)
    }

    private fun disableEkfFlags(){
        for(flagView in ekfFlagsViews.values())
            flagView?.setCompoundDrawablesWithIntrinsicBounds(null, null, unknownFlagDrawable, null)
    }

    private fun updateEkfFlags(ekfStatus: EkfStatus){
        for((flag, flagView) in ekfFlagsViews){
            val isFlagSet = if(flag === EkfStatus.EkfFlags.EKF_CONST_POS_MODE) !ekfStatus.isEkfFlagSet(flag) else ekfStatus.isEkfFlagSet(flag)
            val flagDrawable = if(isFlagSet) okFlagDrawable else badFlagDrawable
            flagView?.setCompoundDrawablesWithIntrinsicBounds(null, null, flagDrawable, null)
        }
    }

    private fun setupEkfFlags(view: View){
        ekfFlagsViews.put(EkfStatus.EkfFlags.EKF_ATTITUDE, view.findViewById(R.id.ekf_attitude_flag) as TextView?)
        ekfFlagsViews.put(EkfStatus.EkfFlags.EKF_CONST_POS_MODE, view.findViewById(R.id.ekf_const_pos_flag) as TextView?)
        ekfFlagsViews.put(EkfStatus.EkfFlags.EKF_VELOCITY_VERT, view.findViewById(R.id.ekf_velocity_vert_flag) as TextView?)
        ekfFlagsViews.put(EkfStatus.EkfFlags.EKF_VELOCITY_HORIZ, view.findViewById(R.id.ekf_velocity_horiz_flag) as TextView?)
        ekfFlagsViews.put(EkfStatus.EkfFlags.EKF_POS_HORIZ_REL, view.findViewById(R.id.ekf_position_horiz_rel_flag) as TextView?)
        ekfFlagsViews.put(EkfStatus.EkfFlags.EKF_POS_HORIZ_ABS, view.findViewById(R.id.ekf_position_horiz_abs_flag) as TextView?)
        ekfFlagsViews.put(EkfStatus.EkfFlags.EKF_PRED_POS_HORIZ_ABS, view.findViewById(R.id.ekf_position_horiz_pred_abs_flag) as TextView?)
        ekfFlagsViews.put(EkfStatus.EkfFlags.EKF_PRED_POS_HORIZ_REL, view.findViewById(R.id.ekf_position_horiz_pred_rel_flag) as TextView?)
        ekfFlagsViews.put(EkfStatus.EkfFlags.EKF_POS_VERT_AGL, view.findViewById(R.id.ekf_position_vert_agl_flag) as TextView?)
        ekfFlagsViews.put(EkfStatus.EkfFlags.EKF_POS_VERT_ABS, view.findViewById(R.id.ekf_position_vert_abs_flag) as TextView?)
    }
}