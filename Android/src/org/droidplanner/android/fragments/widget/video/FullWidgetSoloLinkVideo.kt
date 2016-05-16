package org.droidplanner.android.fragments.widget.video

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.TextView
import com.o3dr.android.client.apis.GimbalApi
import com.o3dr.android.client.apis.solo.SoloCameraApi
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent
import com.o3dr.services.android.lib.drone.attribute.AttributeType
import com.o3dr.services.android.lib.drone.companion.solo.SoloAttributes
import com.o3dr.services.android.lib.drone.companion.solo.SoloEvents
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloGoproConstants
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloGoproState
import com.o3dr.services.android.lib.drone.property.Attitude
import com.o3dr.services.android.lib.model.AbstractCommandListener
import org.droidplanner.android.R
import org.droidplanner.android.dialogs.LoadingDialog
import timber.log.Timber

/**
 * Created by Fredia Huya-Kouadio on 7/19/15.
 */
public class FullWidgetSoloLinkVideo : BaseVideoWidget() {

    companion object {
        private val filter = initFilter()

        @JvmStatic protected val TAG = FullWidgetSoloLinkVideo::class.java.simpleName

        private fun initFilter(): IntentFilter {
            val temp = IntentFilter()
            temp.addAction(AttributeEvent.STATE_CONNECTED)
            temp.addAction(SoloEvents.SOLO_GOPRO_STATE_UPDATED)
            return temp
        }
    }

    private val handler = Handler()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                AttributeEvent.STATE_CONNECTED -> {
                    tryStreamingVideo()
                    onGoproStateUpdate()
                }

                SoloEvents.SOLO_GOPRO_STATE_UPDATED -> {
                    onGoproStateUpdate()
                }
            }
        }

    }

    private val resetGimbalControl = object: Runnable {

        override fun run() {
            if (drone != null) {
                GimbalApi.getApi(drone).stopGimbalControl(orientationListener)
            }
            handler.removeCallbacks(this)
        }
    }

    private var fpvLoader: LoadingDialog? = null

    private var surfaceRef: Surface? = null

    private val textureView by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById(R.id.sololink_video_view) as TextureView?
    }

    private val videoStatus by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById(R.id.sololink_video_status) as TextView?
    }

    private val widgetButtonBar by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById(R.id.widget_button_bar)
    }

    private val takePhotoButton by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById(R.id.sololink_take_picture_button)
    }

    private val recordVideo by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById(R.id.sololink_record_video_button)
    }

    private val fpvVideo by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById(R.id.sololink_vr_video_button)
    }

    private val touchCircleImage by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById(R.id.sololink_gimbal_joystick)
    }

    private val orientationListener = object : GimbalApi.GimbalOrientationListener {
        override fun onGimbalOrientationUpdate(orientation: GimbalApi.GimbalOrientation) {
        }

        override fun onGimbalOrientationCommandError(code: Int) {
            Timber.e("command failed with error code: %d", code)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_widget_sololink_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textureView?.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
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

        takePhotoButton?.setOnClickListener {
            Timber.d("Taking photo.. cheeze!")
            val drone = drone
            if (drone != null) {
                //TODO: fix when camera control support is stable on sololink
                SoloCameraApi.getApi(drone).takePhoto(null)
            }
        }

        recordVideo?.setOnClickListener {
            Timber.d("Recording video!")
            val drone = drone
            if (drone != null) {
                //TODO: fix when camera control support is stable on sololink
                SoloCameraApi.getApi(drone).toggleVideoRecording(null)
            }
        }

        fpvVideo?.setOnClickListener {
            launchFpvApp()
        }
    }

    private fun launchFpvApp() {
        val appId = "meavydev.DronePro"

        //Check if the dronepro app is installed.
        val activity = activity ?: return
        val pm = activity.getPackageManager()
        var launchIntent: Intent? = pm.getLaunchIntentForPackage(appId)
        if (launchIntent == null) {

            //Search for the dronepro app in the play store
            launchIntent = Intent(Intent.ACTION_VIEW).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).setData(Uri.parse("market://details?id=" + appId))

            if (pm.resolveActivity(launchIntent, PackageManager.MATCH_DEFAULT_ONLY) == null) {
                launchIntent = Intent(Intent.ACTION_VIEW).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).setData(Uri.parse("https://play.google.com/store/apps/details?id=" + appId))
            }

            startActivity(launchIntent)

        } else {
            if(fpvLoader == null) {
                launchIntent.putExtra("meavydev.DronePro.launchFPV", "Tower")

                fpvLoader = LoadingDialog.newInstance("Starting FPV...", object : LoadingDialog.Listener {
                    override fun onStarted() {
                        handler.postDelayed( {startActivity(launchIntent) }, 500L)
                    }

                    override fun onCancel() {
                        fpvLoader = null
                    }

                    override fun onDismiss() {
                        fpvLoader = null
                    }

                });
                fpvLoader?.show(childFragmentManager, "FPV launch dialog")
            }
        }
    }

    override fun onApiConnected() {
        tryStreamingVideo()
        onGoproStateUpdate()
        broadcastManager.registerReceiver(receiver, filter)
    }

    override fun onResume() {
        super.onResume()
        tryStreamingVideo()
    }

    override fun onPause() {
        super.onPause()
        tryStoppingVideoStream()
    }

    override fun onStop(){
        super.onStop()
        fpvLoader?.dismiss()
        fpvLoader = null
    }

    override fun onApiDisconnected() {
        tryStoppingVideoStream()
        onGoproStateUpdate()
        broadcastManager.unregisterReceiver(receiver)
    }

    private fun tryStreamingVideo() {
        if (surfaceRef == null)
            return

        val drone = drone
        videoStatus?.visibility = View.GONE

        startVideoStream(surfaceRef!!, TAG, object : AbstractCommandListener() {
            override fun onError(error: Int) {
                Timber.d("Unable to start video stream: %d", error)
                GimbalApi.getApi(drone).stopGimbalControl(orientationListener)
                textureView?.setOnTouchListener(null)
                videoStatus?.visibility = View.VISIBLE
            }

            override fun onSuccess() {
                videoStatus?.visibility = View.GONE
                Timber.d("Video stream started successfully")

                val gimbalTracker = object : View.OnTouchListener {
                    var startX: Float = 0f
                    var startY: Float = 0f

                    override fun onTouch(view: View, event: MotionEvent): Boolean {
                        return moveCopter(view, event)
                    }

                    private fun yawRotateTo(view: View, event: MotionEvent): Double {
                        val drone = drone ?: return -1.0

                        val attitude = drone.getAttribute<Attitude>(AttributeType.ATTITUDE)
                        var currYaw = attitude.getYaw()

                        //yaw value is between -180 and 180. Convert so the value is between 0 to 360
                        if (currYaw < 0) {
                            currYaw += 360.0
                        }

                        val degreeIntervals = (360f / view.width).toDouble()
                        val rotateDeg = (degreeIntervals * (event.x - startX)).toFloat()
                        var rotateTo = currYaw.toFloat() + rotateDeg

                        //Ensure value stays in range between 0 and 360
                        rotateTo = (rotateTo + 360) % 360
                        return rotateTo.toDouble()
                    }

                    private fun moveCopter(view: View, event: MotionEvent): Boolean {
                        val xTouch = event.x
                        val yTouch = event.y

                        val touchWidth = touchCircleImage?.width ?: 0
                        val touchHeight = touchCircleImage?.height ?: 0
                        val centerTouchX = (touchWidth / 2f).toFloat()
                        val centerTouchY = (touchHeight / 2f).toFloat()

                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                handler.removeCallbacks(resetGimbalControl)
                                GimbalApi.getApi(drone).startGimbalControl(orientationListener)

                                touchCircleImage?.setVisibility(View.VISIBLE)
                                touchCircleImage?.setX(xTouch - centerTouchX)
                                touchCircleImage?.setY(yTouch - centerTouchY)
                                startX = event.x
                                startY = event.y
                                return true
                            }
                            MotionEvent.ACTION_MOVE -> {
                                val yawRotateTo = yawRotateTo(view, event).toFloat()
                                sendYawAndPitch(view, event, yawRotateTo)
                                touchCircleImage?.setVisibility(View.VISIBLE)
                                touchCircleImage?.setX(xTouch - centerTouchX)
                                touchCircleImage?.setY(yTouch - centerTouchY)
                                return true
                            }
                            MotionEvent.ACTION_UP -> {
                                touchCircleImage?.setVisibility(View.GONE)
                                handler.postDelayed(resetGimbalControl, 3500L)
                            }
                        }
                        return false
                    }

                    private fun sendYawAndPitch(view: View, event: MotionEvent, rotateTo: Float) {
                        val orientation = GimbalApi.getApi(drone).getGimbalOrientation()

                        val degreeIntervals = 90f / view.height
                        val pitchDegree = (degreeIntervals * (startY - event.y)).toFloat()
                        val pitchTo = orientation.getPitch() + pitchDegree

                        Timber.d("Pitch %f roll %f yaw %f", orientation.getPitch(), orientation.getRoll(), rotateTo)
                        Timber.d("degreeIntervals: %f pitchDegree: %f, pitchTo: %f", degreeIntervals, pitchDegree, pitchTo)

                        GimbalApi.getApi(drone).updateGimbalOrientation(pitchTo, orientation.getRoll(), rotateTo, orientationListener)
                    }
                }

                textureView?.setOnTouchListener(gimbalTracker)
            }

            override fun onTimeout() {
                Timber.d("Timed out while trying to start the video stream")
                GimbalApi.getApi(drone).stopGimbalControl(orientationListener)
                textureView?.setOnTouchListener(null)
                videoStatus?.visibility = View.VISIBLE
            }

        })
    }

    private fun tryStoppingVideoStream() {
        val drone = drone

        stopVideoStream(TAG, object : AbstractCommandListener() {
            override fun onError(error: Int) {
                Timber.d("Unable to stop video stream: %d", error)
            }

            override fun onSuccess() {
                Timber.d("Video streaming stopped successfully.")
                GimbalApi.getApi(drone).stopGimbalControl(orientationListener)
            }

            override fun onTimeout() {
                Timber.d("Timed out while stopping video stream.")
            }

        })
    }

    private fun onGoproStateUpdate() {
        val goproState: SoloGoproState? = drone?.getAttribute(SoloAttributes.SOLO_GOPRO_STATE)
        if (goproState == null) {
            widgetButtonBar?.visibility = View.GONE
        } else {
            widgetButtonBar?.visibility = View.VISIBLE

            //Update the video recording button
            recordVideo?.isActivated = goproState.captureMode == SoloGoproConstants.CAPTURE_MODE_VIDEO
                    && goproState.recording == SoloGoproConstants.RECORDING_ON
        }
    }

    private fun adjustAspectRatio(textureView: TextureView) {
        val viewWidth = textureView.width
        val viewHeight = textureView.height
        val aspectRatio: Float = 9f / 16f

        val newWidth: Int
        val newHeight: Int
        if (viewHeight > (viewWidth * aspectRatio)) {
            //limited by narrow width; restrict height
            newWidth = viewWidth
            newHeight = (viewWidth * aspectRatio).toInt()
        } else {
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