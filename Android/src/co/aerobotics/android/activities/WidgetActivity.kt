package co.aerobotics.android.activities

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.Fragment
import co.aerobotics.android.fragments.widget.TowerWidget
import co.aerobotics.android.fragments.widget.TowerWidgets
import co.aerobotics.android.fragments.widget.video.FullWidgetSoloLinkVideo

/**
 * Created by Fredia Huya-Kouadio on 7/19/15.
 */
class WidgetActivity : co.aerobotics.android.activities.helpers.SuperUI() {
    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        const val EXTRA_WIDGET_ID = "extra_widget_id"
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(co.aerobotics.android.R.layout.activity_widget)

        val fm = supportFragmentManager
        var flightDataFragment = fm.findFragmentById(co.aerobotics.android.R.id.map_view) as co.aerobotics.android.fragments.FlightDataFragment?
        if(flightDataFragment == null){
            flightDataFragment = co.aerobotics.android.fragments.FlightDataFragment()
            fm.beginTransaction().add(co.aerobotics.android.R.id.map_view, flightDataFragment).commit()
        }

        handleIntent(intent)
    }

    override fun addToolbarFragment() {
        val toolbarId = toolbarId
        val fm = supportFragmentManager
        var actionBarTelem: Fragment? = fm.findFragmentById(toolbarId)
        if (actionBarTelem == null) {
            actionBarTelem = co.aerobotics.android.fragments.actionbar.ActionBarTelemFragment()
            fm.beginTransaction().add(toolbarId, actionBarTelem).commit()
        }
    }

    override fun onNewIntent(intent: Intent?){
        super.onNewIntent(intent)
        if(intent != null)
            handleIntent(intent)
    }

    private fun handleIntent(intent: Intent){
        val widgetId = intent.getIntExtra(co.aerobotics.android.activities.WidgetActivity.Companion.EXTRA_WIDGET_ID, 0)
        val fm = supportFragmentManager

        val widget = TowerWidgets.getWidgetById(widgetId)
        if(widget != null){
            //setToolbarTitle(widget.labelResId)

            val currentWidgetType = (fm.findFragmentById(co.aerobotics.android.R.id.widget_view) as TowerWidget?)?.getWidgetType()

            if(widget == currentWidgetType)
                return

            val widgetFragment = widget.getMaximizedFragment()
            fm.beginTransaction().replace(co.aerobotics.android.R.id.widget_view, widgetFragment).commit()
        }
    }

    override fun getToolbarId() = co.aerobotics.android.R.id.actionbar_container

}