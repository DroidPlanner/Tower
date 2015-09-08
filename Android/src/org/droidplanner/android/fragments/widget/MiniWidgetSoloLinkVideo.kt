package org.droidplanner.android.fragments.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.GestureDetectorCompat
import android.view.*
import android.widget.TextView
import com.o3dr.android.client.apis.CapabilityApi
import com.o3dr.android.client.apis.CapabilityApi.FeatureIds
import com.o3dr.android.client.apis.GimbalApi
import com.o3dr.android.client.apis.solo.SoloCameraApi
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent
import com.o3dr.services.android.lib.model.AbstractCommandListener
import com.o3dr.services.android.lib.model.SimpleCommandListener
import org.droidplanner.android.R
import org.droidplanner.android.fragments.helpers.ApiListenerFragment
import org.droidplanner.android.fragments.widget.TowerWidget
import org.droidplanner.android.fragments.widget.TowerWidgets
import timber.log.Timber
import kotlin.platform.platformStatic
import kotlin.properties.Delegates

/**
 * Created by Fredia Huya-Kouadio on 7/19/15.
 */
public class MiniWidgetSoloLinkVideo : TowerWidget() {

    companion object {
        private val filter = IntentFilter(AttributeEvent.STATE_CONNECTED)

        private val TAG = javaClass<MiniWidgetSoloLinkVideo>().getSimpleName()
    }

    private val receiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.getAction()){
                AttributeEvent.STATE_CONNECTED -> tryStreamingVideo()
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

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_mini_widget_solo_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        textureView?.setSurfaceTextureListener(object : TextureView.SurfaceTextureListener{
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

        })
    }

    override fun onApiConnected() {
        tryStreamingVideo()
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
        getBroadcastManager().unregisterReceiver(receiver)
    }

    private fun tryStreamingVideo(){
        if(surfaceRef == null) {
            return
        }

        val drone = getDrone()
        videoStatus?.setVisibility(View.GONE)

        Timber.d("Starting video stream with tag %s", TAG)
        SoloCameraApi.getApi(drone).startVideoStream(surfaceRef, TAG, object : AbstractCommandListener(){
            override fun onError(error: Int) {
                Timber.d("Unable to start video stream: %d", error)
                videoStatus?.setVisibility(View.VISIBLE)
            }

            override fun onSuccess() {
                Timber.d("Video stream started successfully")
                videoStatus?.setVisibility(View.GONE)
            }

            override fun onTimeout() {
                Timber.d("Timed out while trying to start the video stream")
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

    override fun getWidgetType() = TowerWidgets.SOLO_VIDEO

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