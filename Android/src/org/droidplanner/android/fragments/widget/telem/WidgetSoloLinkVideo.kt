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
import com.o3dr.services.android.lib.model.SimpleCommandListener
import org.droidplanner.android.R
import org.droidplanner.android.fragments.helpers.ApiListenerFragment
import kotlin.properties.Delegates

/**
 * Created by Fredia Huya-Kouadio on 7/19/15.
 */
public class WidgetSoloLinkVideo : ApiListenerFragment() {

    companion object {
        val filter = IntentFilter(AttributeEvent.STATE_CONNECTED)
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

    override fun onApiDisconnected() {
        tryStoppingVideoStream()
        getBroadcastManager().unregisterReceiver(receiver)
    }

    private fun tryStreamingVideo(){
        val drone = getDrone()
        CapabilityApi.getApi(drone).checkFeatureSupport(FeatureIds.SOLOLINK_VIDEO_STREAMING, {featureId, result, bundle ->
            when(result){
                CapabilityApi.FEATURE_SUPPORTED -> {
                    videoStatus?.setVisibility(View.GONE)

                    textureView?.setSurfaceTextureListener(object : TextureView.SurfaceTextureListener{
                        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
                            SoloLinkApi.getApi(drone).startVideoStream(Surface(surface), object : SimpleCommandListener(){

                            })
                        }

                        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                            return true
                        }

                        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {

                        }

                        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {

                        }

                    })
                }

                else -> {
                    videoStatus?.setVisibility(View.VISIBLE)
                }
            }
        })
    }

    private fun tryStoppingVideoStream(){
        val drone = getDrone()
        SoloLinkApi.getApi(drone).stopVideoStream(null)
    }
}