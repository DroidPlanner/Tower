package org.droidplanner.android.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import com.o3dr.android.client.apis.CapabilityApi
import com.o3dr.android.client.apis.SoloLinkApi
import com.o3dr.android.client.apis.VehicleApi
import com.o3dr.services.android.lib.coordinate.LatLong
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent
import org.droidplanner.android.R
import org.droidplanner.android.activities.helpers.SuperUI
import org.droidplanner.android.fragments.FlightMapFragment
import org.droidplanner.android.utils.prefs.AutoPanMode
import org.droidplanner.android.fragments.widget.telem.WidgetSoloLinkVideo
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
            return temp
        }
    }

    private val receiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.getAction()){
                AttributeEvent.STATE_CONNECTED -> checkSoloLinkVideoSupport()
                AttributeEvent.STATE_DISCONNECTED -> finish()
            }
        }

    }

    private val goToMyLocation by Delegates.lazy {
        findViewById(R.id.my_location_button) as ImageButton?
    }

    private val goToDroneLocation by Delegates.lazy {
        findViewById(R.id.drone_location_button) as ImageButton?
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
                SoloLinkApi.getApi(drone).takePhoto(null)
            }
        }

        recordVideo?.setOnClickListener {
            val drone = dpApp.getDrone()
            if(drone != null){
                //TODO: fix when camera control support is stable on sololink
                SoloLinkApi.getApi(drone).toggleVideoRecording(null)
            }
        }

        handleIntent(getIntent())
    }

    override fun onNewIntent(intent: Intent?){
        super.onNewIntent(intent)
        if(intent != null)
            handleIntent(intent)
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

    protected override fun initToolbar(){
        val toolbar = findViewById(R.id.actionbar_container) as Toolbar?
        setSupportActionBar(toolbar)

        super.initToolbar()

        getSupportActionBar()?.setTitle("SoloLink Video")
    }

    private fun checkSoloLinkVideoSupport(){
        val drone = dpApp.getDrone()
        if(drone == null || !drone.isConnected())
            finish()
        else{
            CapabilityApi.getApi(drone).checkFeatureSupport(CapabilityApi.FeatureIds.SOLOLINK_VIDEO_STREAMING, { featureId, result, bundle ->
                when (result) {
                    CapabilityApi.FEATURE_SUPPORTED -> {}
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

    override fun isDisplayTitleEnabled() = true
}