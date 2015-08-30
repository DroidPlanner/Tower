package org.droidplanner.android.fragments.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import com.o3dr.android.client.Drone
import com.o3dr.android.client.apis.GimbalApi
import com.o3dr.android.client.apis.solo.SoloCameraApi
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent
import com.o3dr.services.android.lib.drone.companion.solo.SoloAttributes
import com.o3dr.services.android.lib.drone.companion.solo.SoloEvents
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloGoproState
import com.o3dr.services.android.lib.model.AbstractCommandListener
import org.droidplanner.android.R
import org.droidplanner.android.fragments.helpers.ApiListenerFragment
import timber.log.Timber
import kotlin.properties.Delegates

/**
 * Created by Fredia Huya-Kouadio on 7/19/15.
 */
public class WidgetSoloLinkVideo : TowerWidget() {

    companion object {
        private val filter = initFilter()

        private val TAG = javaClass<WidgetSoloLinkVideo>().getSimpleName()

        private fun initFilter(): IntentFilter {
            val temp = IntentFilter()
            temp.addAction(AttributeEvent.STATE_CONNECTED)
            temp.addAction(SoloEvents.SOLO_GOPRO_STATE_UPDATED)
            return temp
        }
    }

    private val receiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.getAction()){
                AttributeEvent.STATE_CONNECTED -> tryStreamingVideo()
                SoloEvents.SOLO_GOPRO_STATE_UPDATED -> {
                    checkGoproControlSupport(getDrone())
                }
            }
        }

    }

    private var surfaceRef: Surface? = null

    private val textureView by Delegates.lazy {
        getView()?.findViewById(R.id.sololink_video_view) as TextureView?
    }

    private val videoStatus by Delegates.lazy {
        getView()?.findViewById(R.id.sololink_video_status) as TextView?
    }

    private val widgetButtonBar by Delegates.lazy {
        getView()?.findViewById(R.id.widget_button_bar)
    }

    private val takePhotoButton by Delegates.lazy {
        getView()?.findViewById(R.id.sololink_take_picture_button) as Button?
    }

    private val recordVideo by Delegates.lazy {
        getView()?.findViewById(R.id.sololink_record_video_button) as Button?
    }

    private val orientationListener = object : GimbalApi.GimbalOrientationListener {
        override fun onGimbalOrientationUpdate(orientation: GimbalApi.GimbalOrientation){
        }

        override fun onGimbalOrientationCommandError(code: Int){
            Timber.e("command failed with error code: %d", code)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_widget_sololink_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        textureView?.setSurfaceTextureListener(object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
                adjustAspectRatio(textureView as TextureView);
                surfaceRef = Surface(surface)
                tryStreamingVideo()
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                surfaceRef = null
                tryStoppingVideoStream()
                return true
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
            }

        }
        )

        takePhotoButton?.setOnClickListener {
            val drone = getDrone()
            if(drone != null) {
                //TODO: fix when camera control support is stable on sololink
                SoloCameraApi.getApi(drone).takePhoto(null)
            }
        }

        recordVideo?.setOnClickListener {
            val drone = getDrone()
            if(drone != null){
                //TODO: fix when camera control support is stable on sololink
                SoloCameraApi.getApi(drone).toggleVideoRecording(null)
            }
        }
    }

    override fun onApiConnected() {
        tryStreamingVideo()
        checkGoproControlSupport(getDrone())
        getBroadcastManager().registerReceiver(receiver, filter)
    }

    override fun onResume(){
        super.onResume()
        tryStreamingVideo()
    }

    override fun onPause(){
        super.onPause()
        tryStoppingVideoStream()
    }

    override fun onApiDisconnected() {
        tryStoppingVideoStream()
        checkGoproControlSupport(getDrone())
        getBroadcastManager().unregisterReceiver(receiver)
    }

    override fun getWidgetType() = TowerWidgets.SOLO_VIDEO

    private fun tryStreamingVideo() {
        if(surfaceRef == null)
            return

        val drone = getDrone()
        videoStatus?.setVisibility(View.GONE)

        Timber.d("Starting video stream with tag %s", TAG)
        SoloCameraApi.getApi(drone).startVideoStream(surfaceRef, TAG, object : AbstractCommandListener() {
            override fun onError(error: Int) {
                Timber.d("Unable to start video stream: %d", error)
                textureView?.setOnTouchListener(null)
                videoStatus?.setVisibility(View.VISIBLE)
            }

            override fun onSuccess() {
                videoStatus?.setVisibility(View.GONE)
                Timber.d("Video stream started successfully")

                val gimbalTracker = object : View.OnTouchListener {
                    var startX: Float = 0f
                    var startY: Float = 0f
                    val gimbalApi = GimbalApi.getApi(drone)
                    val orientation = gimbalApi.getGimbalOrientation()
                    var pitch = orientation.getPitch()
                    var yaw = orientation.getYaw()

                    override fun onTouch(view: View, event: MotionEvent): Boolean {
                        val conversionX = view.getWidth() / 90
                        val conversionY = view.getHeight() / 90
                        when (event.getAction()) {
                            MotionEvent.ACTION_DOWN -> {
                                startX = event.getX()
                                startY = event.getY()
                                gimbalApi.startGimbalControl(orientationListener)
                                return true
                            }
                            MotionEvent.ACTION_MOVE -> {
                                val vX = event.getX() - startX
                                val vY = event.getY() - startY
                                pitch += vY / conversionX
                                yaw += vX / conversionY
                                //                        Timber.d("drag %f, %f", yaw, pitch)
                                gimbalApi.updateGimbalOrientation(pitch, yaw, orientation.getRoll(), orientationListener)
                                startX = event.getX()
                                startY = event.getY()
                                pitch = Math.min(pitch, 0f)
                                pitch = Math.max(pitch, -90f)
                                return true
                            }
                            MotionEvent.ACTION_UP -> gimbalApi.stopGimbalControl(orientationListener)

                        }
                        return false
                    }
                }

                textureView?.setOnTouchListener(gimbalTracker)
            }

            override fun onTimeout() {
                Timber.d("Timed out while trying to start the video stream")
                textureView?.setOnTouchListener(null)
                videoStatus?.setVisibility(View.VISIBLE)
            }

        })
    }

    private fun tryStoppingVideoStream(){
        val drone = getDrone()

        Timber.d("Stopping video stream with tag %s", TAG)
        SoloCameraApi.getApi(drone).stopVideoStream(TAG, object : AbstractCommandListener(){
            override fun onError(error: Int) {
                Timber.d("Unable to stop video stream: %d", error)
            }

            override fun onSuccess() {
                Timber.d("Video streaming stopped successfully.")
            }

            override fun onTimeout() {
                Timber.d("Timed out while stopping video stream.")
            }

        })
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

    private fun adjustAspectRatio(textureView: TextureView){
        val viewWidth = textureView.getWidth()
        val viewHeight = textureView.getHeight()
        val aspectRatio: Float = 9f/16f

        val newWidth: Int
        val newHeight: Int
        if(viewHeight > (viewWidth * aspectRatio)){
            //limited by narrow width; restrict height
            newWidth = viewWidth
            newHeight = (viewWidth * aspectRatio).toInt()
        }
        else{
            //limited by short height; restrict width
            newWidth = (viewHeight / aspectRatio).toInt();
            newHeight = viewHeight
        }

        val xoff = (viewWidth - newWidth) / 2f
        val yoff = (viewHeight - newHeight) / 2f

        val txform = Matrix();
        textureView.getTransform(txform);
        txform.setScale((newWidth.toFloat() / viewWidth), newHeight.toFloat() / viewHeight);

        txform.postTranslate(xoff, yoff);
        textureView.setTransform(txform);
    }
}