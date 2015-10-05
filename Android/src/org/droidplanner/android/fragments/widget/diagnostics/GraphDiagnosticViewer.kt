package org.droidplanner.android.fragments.widget.diagnostics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import lecho.lib.hellocharts.formatter.SimpleColumnChartValueFormatter
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.view.ColumnChartView
import org.droidplanner.android.R
import org.droidplanner.android.fragments.widget.diagnostics.BaseWidgetDiagnostic
import java.util.*

/**
 * Created by Fredia Huya-Kouadio on 9/15/15.
 */
public abstract class GraphDiagnosticViewer : BaseWidgetDiagnostic() {

    protected val disabledColor: Int = Color.parseColor("#ffaaaaaa")
    protected val goodStatusColor: Int = Color.parseColor("#ff669900")
    protected val warningStatusColor: Int = Color.parseColor("#ffffbb33")
    protected val dangerStatusColor: Int = Color.parseColor("#ffcc0000")

    protected var graph: ColumnChartView? = null

    protected val chartData: ColumnChartData = ColumnChartData()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_graph_diagnostic_viewer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupGraph(view)
    }

    protected abstract fun getYAxis(): Axis

    protected abstract fun getXAxis(): Axis

    protected abstract fun getFormatter(): SimpleColumnChartValueFormatter

    protected abstract fun getColumns(): ArrayList<Column>

    protected abstract fun getViewPort(refViewPort: Viewport?): Viewport

    protected fun generateDisabledColumn(): Column {
        val col = Column(listOf(SubcolumnValue(0f, disabledColor)))
        col.setHasLabelsOnlyForSelected(true)
        col.setFormatter(getFormatter())
        return col
    }

    private fun setupGraph(view: View) {
        graph = view.findViewById(R.id.column_chart) as ColumnChartView?
        graph?.isValueSelectionEnabled = true
        graph?.isZoomEnabled = false
        graph?.isViewportCalculationEnabled = false

        val viewPort = getViewPort(graph?.maximumViewport)
        graph?.maximumViewport = viewPort
        graph?.currentViewport = viewPort

        val axisY = getYAxis()
        chartData.axisYLeft = axisY

        val axisX = getXAxis()
        chartData.axisXBottom = axisX

        chartData.setColumns(getColumns())

        graph?.columnChartData = chartData
    }

    protected open fun disableGraph() {
        for (column in chartData.columns) {
            for (value in column.values) {
                value.setTarget(0f)
                value.setColor(disabledColor)
            }
        }

        graph?.startDataAnimation()
    }

    override fun disableEkfView() {
        disableGraph()
    }

    override fun disableVibrationView() {
        disableGraph()
    }
}