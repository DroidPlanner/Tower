package org.droidplanner.android.fragments.widget.diagnostics

import android.os.Bundle
import android.os.Handler
import android.support.annotation.StringRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.o3dr.services.android.lib.drone.property.Vibration
import com.o3dr.services.android.lib.util.SpannableUtils
import lecho.lib.hellocharts.formatter.SimpleAxisValueFormatter
import lecho.lib.hellocharts.formatter.SimpleColumnChartValueFormatter
import lecho.lib.hellocharts.model.Axis
import lecho.lib.hellocharts.model.Viewport
import org.droidplanner.android.R
import org.droidplanner.android.fragments.widget.diagnostics.BaseWidgetDiagnostic

/**
 * Created by Fredia Huya-Kouadio on 9/15/15.
 */
public class VibrationViewer : GraphDiagnosticViewer() {

    companion object {
        @StringRes val LABEL_ID: Int = R.string.title_vibration_viewer
    }

    private val handler = Handler()

    private val lastClippingValues = arrayOf(-1L, -1L, -1L)
    private val clippingViews: Array<TextView?> = arrayOf(null, null, null)

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        return inflater?.inflate(R.layout.fragment_vibration_viewer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        clippingViews.set(0, view.findViewById(R.id.primary_clipping_value) as TextView?)
        clippingViews.set(1, view.findViewById(R.id.secondary_clipping_value) as TextView?)
        clippingViews.set(2, view.findViewById(R.id.tertiary_clipping_value) as TextView?)
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
        val vibrations = listOf(vibration.vibrationX,
                vibration.vibrationY,
                vibration.vibrationZ)

        var maxVibration = 0f
        val cols = chartData.columns
        val colsCount = cols.size() - 1
        for(i in 0..colsCount){
            val vibValue = vibrations.get(i)
            maxVibration = Math.max(maxVibration, vibValue)
            val vibColor = if(vibValue < BaseWidgetDiagnostic.GOOD_VIBRATION_THRESHOLD) goodStatusColor
            else if(vibValue < BaseWidgetDiagnostic.WARNING_VIBRATION_THRESHOLD) warningStatusColor
            else dangerStatusColor

            val col = cols.get(i)
            for(value in col.values){
                value.setTarget(vibValue)
                value.setColor(vibColor)
            }
        }

        graph?.startDataAnimation()

        updateClippingTracker(vibration)

        val parentFragment = parentFragment
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
        disableClippingTracker()

        val parentFragment = parentFragment
        if(parentFragment is FullWidgetDiagnostics){
            parentFragment.setAdapterViewTitle(2, getText(LABEL_ID))
        }
    }

    private fun updateClippingTracker(vibration: Vibration){
        fun updateClippingValue(index: Int, value: Long){
            val clippingView = clippingViews.get(index)
            clippingView?.text = value.toString()

            val lastClippingValue = lastClippingValues[index]
            if(lastClippingValue != -1L && value > lastClippingValue){
                handler.removeCallbacks(null)

                //Update the clipping view background temporarily
                clippingView?.setBackgroundColor(dangerStatusColor)

                handler.postDelayed(object : Runnable {
                    override fun run() {
                        clippingView?.setBackgroundColor(goodStatusColor)
                    }
                }, 1000L)
            }
            else{
                clippingView?.setBackgroundColor(goodStatusColor)
            }

            lastClippingValues[index] = value
        }

        updateClippingValue(0, vibration.firstAccelClipping)
        updateClippingValue(1, vibration.secondAccelClipping)
        updateClippingValue(2, vibration.thirdAccelClipping)
    }

    private fun disableClippingTracker(){
        fun disableClippingValue(index: Int){
            val clippingView = clippingViews.get(index)
            clippingView?.setText(R.string.empty_content)
            clippingView?.background = null

            lastClippingValues.set(index, -1L)
        }

        disableClippingValue(0)
        disableClippingValue(1)
        disableClippingValue(2)
    }
}