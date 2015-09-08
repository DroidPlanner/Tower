package org.droidplanner.android.fragments.widget

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.o3dr.services.android.lib.drone.property.EkfStatus
import com.o3dr.services.android.lib.drone.property.EkfStatus.EkfFlags
import com.o3dr.services.android.lib.util.SpannableUtils
import lecho.lib.hellocharts.formatter.SimpleAxisValueFormatter
import lecho.lib.hellocharts.formatter.SimpleColumnChartValueFormatter
import lecho.lib.hellocharts.model.Axis
import lecho.lib.hellocharts.model.Column
import lecho.lib.hellocharts.model.ColumnChartData
import lecho.lib.hellocharts.model.SubcolumnValue
import lecho.lib.hellocharts.view.ColumnChartView
import org.droidplanner.android.R
import org.droidplanner.android.activities.WidgetActivity
import java.util.*
import kotlin.properties.Delegates

/**
 * Created by Fredia Huya-Kouadio on 8/30/15.
 */
public class FullWidgetEkfStatus : BaseWidgetEkfStatus(){

    companion object {
        private val DECIMAL_DIGITS_NUMBER = 1
    }

    private var ekfGraph: ColumnChartView? = null

    private val ekfFlagsViews: HashMap<EkfFlags, TextView?> = HashMap()

    private val disabledColor = Color.parseColor("#ffaaaaaa")
    private val goodStatusColor = Color.parseColor("#ff669900")
    private val warningStatusColor = Color.parseColor("#ffffbb33")
    private val dangerStatusColor = Color.parseColor("#ffcc0000")

    private val okFlagDrawable: Drawable? by Delegates.lazy {
        getResources()?.getDrawable(R.drawable.ic_check_box_green_500_24dp)
    }

    private val badFlagDrawable: Drawable? by Delegates.lazy {
        getResources()?.getDrawable(R.drawable.ic_cancel_red_500_24dp)
    }

    private val unknownFlagDrawable: Drawable? by Delegates.lazy {
        getResources()?.getDrawable(R.drawable.ic_help_orange_500_24dp)
    }

    private val chartData = ColumnChartData()

    private var parentActivity: WidgetActivity? = null

    override fun onAttach(activity: Activity){
        super.onAttach(activity)
        if(activity is WidgetActivity)
            parentActivity = activity
    }

    override fun onDetach(){
        super.onDetach()
        parentActivity = null
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_full_widget_ekf_status, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        setupEkfGraph(view)
        setupEkfFlags(view)
    }

    private fun setupEkfFlags(view: View){
        ekfFlagsViews.put(EkfFlags.EKF_ATTITUDE, view.findViewById(R.id.ekf_attitude_flag) as TextView?)
        ekfFlagsViews.put(EkfFlags.EKF_CONST_POS_MODE, view.findViewById(R.id.ekf_const_pos_flag) as TextView?)
        ekfFlagsViews.put(EkfFlags.EKF_VELOCITY_VERT, view.findViewById(R.id.ekf_velocity_vert_flag) as TextView?)
        ekfFlagsViews.put(EkfFlags.EKF_VELOCITY_HORIZ, view.findViewById(R.id.ekf_velocity_horiz_flag) as TextView?)
        ekfFlagsViews.put(EkfFlags.EKF_POS_HORIZ_REL, view.findViewById(R.id.ekf_position_horiz_rel_flag) as TextView?)
        ekfFlagsViews.put(EkfFlags.EKF_POS_HORIZ_ABS, view.findViewById(R.id.ekf_position_horiz_abs_flag) as TextView?)
        ekfFlagsViews.put(EkfFlags.EKF_PRED_POS_HORIZ_ABS, view.findViewById(R.id.ekf_position_horiz_pred_abs_flag) as TextView?)
        ekfFlagsViews.put(EkfFlags.EKF_PRED_POS_HORIZ_REL, view.findViewById(R.id.ekf_position_horiz_pred_rel_flag) as TextView?)
        ekfFlagsViews.put(EkfFlags.EKF_POS_VERT_AGL, view.findViewById(R.id.ekf_position_vert_agl_flag) as TextView?)
        ekfFlagsViews.put(EkfFlags.EKF_POS_VERT_ABS, view.findViewById(R.id.ekf_position_vert_abs_flag) as TextView?)
    }

    private fun setupEkfGraph(view: View){
        ekfGraph = view.findViewById(R.id.ekf_column_chart) as ColumnChartView?
        ekfGraph?.setValueSelectionEnabled(true)
        ekfGraph?.setZoomEnabled(false)
        ekfGraph?.setViewportCalculationEnabled(false)

        val viewPort = ekfGraph?.getMaximumViewport()
        viewPort?.bottom = 0f
        viewPort?.top = 1f
        viewPort?.left = -0.5f
        viewPort?.right = 4.5f
        ekfGraph?.setMaximumViewport(viewPort)
        ekfGraph?.setCurrentViewport(viewPort)

        val axisY = Axis.generateAxisFromRange(0f, 1f, 0.1f)
                .setHasLines(true)
                .setFormatter(SimpleAxisValueFormatter(DECIMAL_DIGITS_NUMBER))
        chartData.setAxisYLeft(axisY)

        val axisX = Axis.generateAxisFromCollection(listOf(0f, 1f, 2f, 3f, 4f),
                listOf("vel", "h. pos", "v. pos", "mag", "terrain"))
        chartData.setAxisXBottom(axisX)

        fun generateDisabledColumn(): Column{
            val col = Column(listOf(SubcolumnValue(0f, disabledColor)))
            col.setHasLabelsOnlyForSelected(true)
            col.setFormatter(SimpleColumnChartValueFormatter(DECIMAL_DIGITS_NUMBER))
            return col
        }

        //Create a column for each variance
        val varianceCols = ArrayList<Column>()

        //Velocity column
        varianceCols.add(generateDisabledColumn())

        //Horizontal position column
        varianceCols.add(generateDisabledColumn())

        //Vertical position column
        varianceCols.add(generateDisabledColumn())

        //Compass variance
        varianceCols.add(generateDisabledColumn())

        //Terrain variance
        varianceCols.add(generateDisabledColumn())
        chartData.setColumns(varianceCols)

        ekfGraph?.setColumnChartData(chartData)
    }

    override fun disableEkfView() {
        disableEkfGraph()
        disableEkfFlags()
    }

    private fun disableEkfGraph(){
        for (column in chartData.getColumns()) {
            for (value in column.getValues()) {
                value.setTarget(0f)
                value.setColor(disabledColor)
            }
        }

        ekfGraph?.startDataAnimation()

        parentActivity?.setToolbarTitle(getWidgetType().labelResId)
    }

    private fun disableEkfFlags(){
            for(flagView in ekfFlagsViews.values())
                flagView?.setCompoundDrawablesWithIntrinsicBounds(null, null, unknownFlagDrawable, null)
    }

    override fun updateEkfView(ekfStatus: EkfStatus) {
        updateEkfGraph(ekfStatus)
        updateEkfFlags(ekfStatus)
    }

    private fun updateEkfFlags(ekfStatus: EkfStatus){
        for((flag, flagView) in ekfFlagsViews){
            val isFlagSet = if(flag === EkfFlags.EKF_CONST_POS_MODE) !ekfStatus.isEkfFlagSet(flag) else ekfStatus.isEkfFlagSet(flag)
            val flagDrawable = if(isFlagSet) okFlagDrawable else badFlagDrawable
            flagView?.setCompoundDrawablesWithIntrinsicBounds(null, null, flagDrawable, null)
        }
    }

    private fun updateEkfGraph(ekfStatus: EkfStatus){
        val variances = listOf(ekfStatus.getVelocityVariance(),
                ekfStatus.getHorizontalPositionVariance(),
                ekfStatus.getVerticalPositionVariance(),
                ekfStatus.getCompassVariance(),
                ekfStatus.getTerrainAltitudeVariance())

        var maxVariance = 0f
        val cols = chartData.getColumns()
        val colsCount = cols.size() -1
        for( i in 0..colsCount){
            val variance = variances.get(i)
            maxVariance = Math.max(maxVariance, variance)
            val varianceColor = if (variance < BaseWidgetEkfStatus.GOOD_VARIANCE_THRESHOLD) goodStatusColor
            else if (variance < BaseWidgetEkfStatus.WARNING_VARIANCE_THRESHOLD) warningStatusColor
            else dangerStatusColor

            val col = cols.get(i)
            for (value in col.getValues()) {
                value.setTarget(variance)
                value.setColor(varianceColor)
            }
        }

        ekfGraph?.startDataAnimation()

        val widgetTitle = SpannableUtils.normal(getText(getWidgetType().labelResId), ": ",
                if (maxVariance < BaseWidgetEkfStatus.GOOD_VARIANCE_THRESHOLD) SpannableUtils.color(goodStatusColor,"Green")
                else if (maxVariance < BaseWidgetEkfStatus.WARNING_VARIANCE_THRESHOLD) SpannableUtils.color(warningStatusColor, "Amber")
                else SpannableUtils.color(dangerStatusColor, "Red")
        )

        parentActivity?.setToolbarTitle(widgetTitle)
    }
}