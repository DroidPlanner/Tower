package co.aerobotics.android.fragments.widget.diagnostics

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.aerobotics.android.activities.WidgetActivity
import co.aerobotics.android.fragments.widget.diagnostics.BaseWidgetDiagnostic
import co.aerobotics.android.fragments.widget.TowerWidget
import co.aerobotics.android.fragments.widget.TowerWidgets

/**
 * Created by Fredia Huya-Kouadio on 8/30/15.
 */
class FullWidgetDiagnostics : TowerWidget(){

    private val viewAdapter by lazy(LazyThreadSafetyMode.NONE) {
        DiagnosticViewAdapter(context, childFragmentManager)
    }

    private var tabPageIndicator: co.aerobotics.android.view.viewPager.TabPageIndicator? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(co.aerobotics.android.R.layout.fragment_full_widget_diagnostics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        val viewPager = view.findViewById(co.aerobotics.android.R.id.diagnostics_view_pager) as ViewPager?
        viewPager?.adapter = viewAdapter
        viewPager?.offscreenPageLimit = 2

        tabPageIndicator = view.findViewById(co.aerobotics.android.R.id.pager_title_strip) as co.aerobotics.android.view.viewPager.TabPageIndicator?
        tabPageIndicator?.setViewPager(viewPager)
    }

    override fun getWidgetType() = TowerWidgets.VEHICLE_DIAGNOSTICS

    override fun onApiConnected() {}

    override fun onApiDisconnected() {}

    fun setAdapterViewTitle(position: Int, title: CharSequence){
        viewAdapter.setViewTitles(position, title)
        tabPageIndicator?.notifyDataSetChanged()
    }
}