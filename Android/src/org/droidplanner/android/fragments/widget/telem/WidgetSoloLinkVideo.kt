package org.droidplanner.android.fragments.widget.telem

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.TextView
import com.o3dr.android.client.apis.CapabilityApi
import com.o3dr.android.client.apis.CapabilityApi.FeatureIds
import com.o3dr.android.client.apis.SoloLinkApi
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent
import com.o3dr.services.android.lib.model.AbstractCommandListener
import com.o3dr.services.android.lib.model.SimpleCommandListener
import org.droidplanner.android.R
import org.droidplanner.android.fragments.helpers.ApiListenerFragment
import timber.log.Timber
import kotlin.properties.Delegates

/**
 * Created by Fredia Huya-Kouadio on 7/19/15.
 */
public class WidgetSoloLinkVideo : ApiListenerFragment() {

    companion object {
        private val filter = IntentFilter(AttributeEvent.STATE_CONNECTED)

        private val TAG = javaClass<WidgetSoloLinkVideo>().getSimpleName()
    }

    private val receiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.getAction()){
                AttributeEvent.STATE_CONNECTED -> tryStreamingVideo()
            }
        }

    }

    private val textureView by Delegates.lazy {
        getView()?.findViewById(R.id.sololink_video_view) as TextureView?
    }

    private val videoStatus by Delegates.lazy {
        getView()?.findViewById(R.id.sololink_video_status) as TextView?
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_widget_sololink_video, container, false)
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
        val drone = getDrone()
        videoStatus?.setVisibility(View.GONE)

        textureView?.setSurfaceTextureListener(object : TextureView.SurfaceTextureListener{
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
                Timber.d("Starting video stream with tag %s", TAG)
                SoloLinkApi.getApi(drone).startVideoStream(Surface(surface), TAG, object : AbstractCommandListener(){
                    override fun onError(error: Int) {
                        Timber.d("Unable to start video stream: %d", error)
                    }

                    override fun onSuccess() {
                        Timber.d("Video stream started successfully")
                    }

                    override fun onTimeout() {
                        Timber.d("Timed out while trying to start the video stream")
                    }

                })
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                tryStoppingVideoStream()
                return true
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {

            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {

            }

        })
    }

    private fun tryStoppingVideoStream(){
        val drone = getDrone()
        Timber.d("Stopping video stream with tag %s", TAG)
        SoloLinkApi.getApi(drone).stopVideoStream(TAG, object : AbstractCommandListener(){
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
}