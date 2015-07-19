package org.droidplanner.android.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import org.droidplanner.android.R
import org.droidplanner.android.activities.helpers.SuperUI
import org.droidplanner.android.fragments.FlightMapFragment
import org.droidplanner.android.utils.prefs.AutoPanMode
import kotlin.properties.Delegates

/**
 * Created by Fredia Huya-Kouadio on 7/19/15.
 */
public class WidgetActivity : SuperUI() {

    companion object {
        val filter = IntentFilter()

    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            throw UnsupportedOperationException()
        }
    }

    private val goToMyLocation by Delegates.lazy {
        findViewById(R.id.my_location_button) as ImageButton?
    }

    private val goToDroneLocation by Delegates.lazy {
        findViewById(R.id.drone_location_button) as ImageButton?
    }

    private var mapFragment: FlightMapFragment? = null

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget)

        val fm = getSupportFragmentManager()
        mapFragment = fm.findFragmentById(R.id.map_view) as FlightMapFragment?
        if(mapFragment == null){
            mapFragment = FlightMapFragment()
            fm.beginTransaction().add(R.id.map_view, mapFragment).commit()
        }

        goToMyLocation?.setOnClickListener {
            mapFragment?.goToMyLocation()
            updateMapLocationButtons(AutoPanMode.DISABLED)
        }
        goToMyLocation?.setOnLongClickListener{
            mapFragment?.goToMyLocation()
            updateMapLocationButtons(AutoPanMode.USER)
            true
        }

        goToDroneLocation?.setOnClickListener {
            mapFragment?.goToDroneLocation()
            updateMapLocationButtons(AutoPanMode.DISABLED)
        }
        goToDroneLocation?.setOnLongClickListener {
            mapFragment?.goToDroneLocation()
            updateMapLocationButtons(AutoPanMode.DRONE)
            true
        }
    }

    private fun updateMapLocationButtons(mode: AutoPanMode){
        goToMyLocation?.setActivated(false)
        goToDroneLocation?.setActivated(false)

        mapFragment?.setAutoPanMode(mode)

        when(mode){
            AutoPanMode.DRONE -> goToDroneLocation?.setActivated(true)
            AutoPanMode.USER -> goToMyLocation?.setActivated(true)
        }
    }

    override fun onApiConnected(){
        super.onApiConnected()
        getBroadcastManager().registerReceiver(receiver, filter)
    }

    override fun onApiDisconnected(){
        super.onApiDisconnected()
        getBroadcastManager().unregisterReceiver(receiver)
    }
}