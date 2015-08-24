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
import org.droidplanner.android.fragments.FlightMapFragment
import org.droidplanner.android.fragments.widget.telem.WidgetSoloLinkVideo
import org.droidplanner.android.utils.prefs.AutoPanMode
import kotlin.properties.Delegates

/**
 * Created by Fredia Huya-Kouadio on 7/19/15.
 */
public class WidgetActivity : SuperUI() {

    private val guidedClickListener = object : FlightMapFragment.OnGuidedClickListener{

        override fun onGuidedClick(coord: LatLong?) {
            val drone = dpApp.getDrone()
            if(drone != null)
                VehicleApi.getApi(drone).sendGuidedPoint(coord, false)
        }
    }

    companion object {
        private val filter = initFilter()

        val EXTRA_WIDGET_ID = "extra_widget_id"

        val WIDGET_SOLOLINK_VIDEO = "widget_sololink_video";

        private fun initFilter(): IntentFilter {
            val temp = IntentFilter()
            temp.addAction(AttributeEvent.STATE_CONNECTED)
            temp.addAction(AttributeEvent.STATE_DISCONNECTED)
            temp.addAction(SoloEvents.SOLO_GOPRO_STATE_UPDATED)
            return temp
        }
    }

    private val receiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.getAction()){
                AttributeEvent.STATE_CONNECTED -> checkSoloLinkVideoSupport()
                AttributeEvent.STATE_DISCONNECTED -> finish()
                SoloEvents.SOLO_GOPRO_STATE_UPDATED -> {
                    checkGoproControlSupport(dpApp.getDrone())
                }
            }
        }

    }

    private val widgetButtonBar by Delegates.lazy {
        findViewById(R.id.widget_button_bar)
    }

    private val goToMyLocation by Delegates.lazy {
        findViewById(R.id.my_location_button) as FloatingActionButton?
    }

    private val goToDroneLocation by Delegates.lazy {
        findViewById(R.id.drone_location_button) as FloatingActionButton?
    }

    private val takePhotoButton by Delegates.lazy {
        findViewById(R.id.sololink_take_picture_button) as Button?
    }

    private val recordVideo by Delegates.lazy {
        findViewById(R.id.sololink_record_video_button) as Button?
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

        takePhotoButton?.setOnClickListener {
            val drone = dpApp.getDrone()
            if(drone != null) {
                //TODO: fix when camera control support is stable on sololink
                SoloCameraApi.getApi(drone).takePhoto(null)
            }
        }

        recordVideo?.setOnClickListener {
            val drone = dpApp.getDrone()
            if(drone != null){
                //TODO: fix when camera control support is stable on sololink
                SoloCameraApi.getApi(drone).toggleVideoRecording(null)
            }
        }

        handleIntent(getIntent())
    }

    override fun onNewIntent(intent: Intent?){
        super.onNewIntent(intent)
        if(intent != null)
            handleIntent(intent)
    }

    override fun onStart(){
        super.onStart()
        setToolbarTitle("SoloLink Video")
    }

    private fun handleIntent(intent: Intent){
        val widgetId = intent.getStringExtra(EXTRA_WIDGET_ID)
        val fm = getSupportFragmentManager()

        when(widgetId){
            WIDGET_SOLOLINK_VIDEO -> {
                var widgetFragment = fm.findFragmentById(R.id.widget_view)
                if(!(widgetFragment is WidgetSoloLinkVideo)){
                    widgetFragment = WidgetSoloLinkVideo()
                    fm.beginTransaction().replace(R.id.widget_view, widgetFragment).commit()
                }
            }
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

    override fun getToolbarId() = R.id.actionbar_container

    private fun checkSoloLinkVideoSupport(){
        val drone = dpApp.getDrone()
        if(drone == null || !drone.isConnected())
            finish()
        else{
            CapabilityApi.getApi(drone).checkFeatureSupport(CapabilityApi.FeatureIds.SOLO_VIDEO_STREAMING, { featureId, result, bundle ->
                when (result) {
                    CapabilityApi.FEATURE_SUPPORTED -> {
                        checkGoproControlSupport(drone)
                    }

                    else -> finish()
                }
            })
        }
    }

    override fun onApiConnected(){
        super.onApiConnected()
        checkSoloLinkVideoSupport()
        mapFragment?.setGuidedClickListener(guidedClickListener)
        getBroadcastManager().registerReceiver(receiver, filter)
    }

    override fun onApiDisconnected(){
        super.onApiDisconnected()
        if(!isFinishing())
            checkSoloLinkVideoSupport()

        mapFragment?.setGuidedClickListener(null)
        getBroadcastManager().unregisterReceiver(receiver)
    }

    private fun checkGoproControlSupport(drone: Drone){
        val goproState: SoloGoproState? = drone.getAttribute(SoloAttributes.SOLO_GOPRO_STATE)
        widgetButtonBar?.setVisibility(
                if (goproState == null)
                    View.GONE
                else
                    View.VISIBLE
        )
    }
}