package org.droidplanner.android.fragments.widget.diagnostics

import android.support.annotation.StringRes
import com.o3dr.services.android.lib.drone.property.Vibration
import com.o3dr.services.android.lib.util.SpannableUtils
import lecho.lib.hellocharts.formatter.SimpleAxisValueFormatter
import lecho.lib.hellocharts.formatter.SimpleColumnChartValueFormatter
import lecho.lib.hellocharts.model.Axis
import lecho.lib.hellocharts.model.Viewport
import org.droidplanner.android.R
import org.droidplanner.android.fragments.widget.BaseWidgetDiagnostic

/**
 * Created by Fredia Huya-Kouadio on 9/15/15.
 */
public class VibrationViewer : GraphDiagnosticViewer() {

    companion object {
        @StringRes public val LABEL_ID: Int = R.string.title_vibration_viewer
    }

    override fun getColumns() = arrayListOf(generateDisabledColumn(), //X axis vibration
            generateDisabledColumn(), //Y axis vibration
            generateDisabledColumn()) // Z axis vibration

    override fun getFormatter() = SimpleColumnChartValueFormatter()

    override fun getViewPort(refViewPort: Viewport?): Viewport {
        val viewPort = refViewPort?: Viewport()
        viewPort.bottom = 0f
        viewPort.top = 100f
        viewPort.left = -0.5f
        viewPort.right = 2.5f

        return viewPort
    }

    override fun getXAxis() = Axis.generateAxisFromCollection(listOf(0f, 1f, 2f),
            listOf("X", "Y", "Z"))

    override fun getYAxis() = Axis.generateAxisFromRange(0f, 100f, 10f)
            .setHasLines(true)
            .setFormatter(SimpleAxisValueFormatter())

    override fun updateVibrationView(vibration: Vibration){
        val vibrations = listOf(vibration.getVibrationX(),
                vibration.getVibrationY(),
                vibration.getVibrationZ())

        var maxVibration = 0f
        val cols = chartData.getColumns()
        val colsCount = cols.size() - 1
        for(i in 0..colsCount){
            val vibValue = vibrations.get(i)
            maxVibration = Math.max(maxVibration, vibValue)
            val vibColor = if(vibValue < BaseWidgetDiagnostic.GOOD_VIBRATION_THRESHOLD) goodStatusColor
            else if(vibValue < BaseWidgetDiagnostic.WARNING_VIBRATION_THRESHOLD) warningStatusColor
            else dangerStatusColor

            val col = cols.get(i)
            for(value in col.getValues()){
                value.setTarget(vibValue)
                value.setColor(vibColor)
            }
        }

        graph?.startDataAnimation()

        val parentFragment = getParentFragment()
        if(parentFragment is FullWidgetDiagnostics){
            val label = getText(LABEL_ID)
            val widgetTitle =
                    if (maxVibration < BaseWidgetDiagnostic.GOOD_VIBRATION_THRESHOLD) SpannableUtils.color(goodStatusColor, label)
                    else if (maxVibration < BaseWidgetDiagnostic.WARNING_VIBRATION_THRESHOLD) SpannableUtils.color(warningStatusColor, label)
                    else SpannableUtils.color(dangerStatusColor, label)

            parentFragment.setAdapterViewTitle(2, widgetTitle)
        }
    }

    override fun disableGraph(){
        super.disableGraph()

        val parentFragment = getParentFragment()
        if(parentFragment is FullWidgetDiagnostics){
            parentFragment.setAdapterViewTitle(2, getText(LABEL_ID))
        }
    }
}