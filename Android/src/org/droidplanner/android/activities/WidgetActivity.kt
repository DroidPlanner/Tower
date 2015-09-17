package org.droidplanner.android.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import com.o3dr.android.client.Drone
import com.o3dr.android.client.apis.CapabilityApi
import com.o3dr.android.client.apis.VehicleApi
import com.o3dr.android.client.apis.solo.SoloCameraApi
import com.o3dr.services.android.lib.coordinate.LatLong
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent
import com.o3dr.services.android.lib.drone.attribute.AttributeType
import com.o3dr.services.android.lib.drone.companion.solo.SoloAttributes
import com.o3dr.services.android.lib.drone.companion.solo.SoloEvents
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloGoproState
import org.droidplanner.android.R
import org.droidplanner.android.activities.helpers.SuperUI
import org.droidplanner.android.fragments.FlightDataFragment
import org.droidplanner.android.fragments.FlightMapFragment
import org.droidplanner.android.fragments.widget.TowerWidget
import org.droidplanner.android.fragments.widget.TowerWidgets
import org.droidplanner.android.fragments.widget.FullWidgetSoloLinkVideo
import org.droidplanner.android.utils.prefs.AutoPanMode
import kotlin.properties.Delegates

/**
 * Created by Fredia Huya-Kouadio on 7/19/15.
 */
public class WidgetActivity : SuperUI() {

    companion object {
        val EXTRA_WIDGET_ID = "extra_widget_id"
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget)

        val fm = supportFragmentManager
        var flightDataFragment = fm.findFragmentById(R.id.map_view) as FlightDataFragment?
        if(flightDataFragment == null){
            flightDataFragment = FlightDataFragment()
            fm.beginTransaction().add(R.id.map_view, flightDataFragment).commit()
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?){
        super.onNewIntent(intent)
        if(intent != null)
            handleIntent(intent)
    }

    private fun handleIntent(intent: Intent){
        val widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, 0)
        val fm = supportFragmentManager

        val widget = TowerWidgets.getWidgetById(widgetId)
        if(widget != null){
            setToolbarTitle(widget.labelResId)

            val currentWidget = fm.findFragmentById(R.id.widget_view) as TowerWidget?
            val currentWidgetType = if(currentWidget == null) null else currentWidget.getWidgetType()

            if(widget == currentWidgetType)
                return

            val widgetFragment = widget.getMaximizedFragment()
            fm.beginTransaction().replace(R.id.widget_view, widgetFragment).commit()
        }
    }

    override fun getToolbarId() = R.id.actionbar_container

}