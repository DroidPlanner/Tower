package org.droidplanner.android.fragments.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.hardware.usb.UsbDevice
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent
import com.o3dr.services.android.lib.drone.companion.solo.SoloEvents
import com.serenegiant.usb.CameraDialog
import com.serenegiant.usb.USBMonitor
import com.serenegiant.usb.UVCCamera
import org.droidplanner.android.R
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates


public class MiniWidgetUVCLinkVideo : TowerWidget() {
    private val DEBUG = true

    companion object {
        private val filter = initFilter()

        private val TAG = MiniWidgetUVCLinkVideo::class.java.simpleName

        private fun initFilter(): IntentFilter {
            val temp = IntentFilter()
            temp.addAction(AttributeEvent.STATE_CONNECTED)
            temp.addAction(AttributeEvent.STATE_ARMING)
            return temp
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                AttributeEvent.STATE_CONNECTED -> {
                    if (!isActive && !isPreview){
                        CameraDialog.showDialog(activity,mUSBMonitor);
                    }
                }
                AttributeEvent.STATE_ARMING -> {
                    if (!isActive && !isPreview){
                        //CameraDialog.showDialog(activity,mUSBMonitor);
                    }
                }
            }
        }
    }

    override fun getWidgetType() = TowerWidgets.UVC_VIDEO

    private var surfaceRef: Surface? = null

    // for thread pool
    private val CORE_POOL_SIZE = 1        // initial/minimum threads
    private val MAX_POOL_SIZE = 4            // maximum threads
    private val KEEP_ALIVE_TIME = 10        // time periods while keep the idle thread
    protected val EXECUTER: ThreadPoolExecutor = ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME.toLong(),
            TimeUnit.SECONDS, LinkedBlockingQueue<Runnable>())


    private val mSync = Object()
    // for accessing USB and USB camera
    private var mUSBMonitor: USBMonitor? = null
    private var mUVCCamera: UVCCamera? = null
    private var isActive: Boolean = false
    private var isPreview:Boolean = false
    private var usbDevice:UsbDevice? = null

    private val textureView by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById(R.id.uvc_video_view) as TextureView?
    }

    private val videoStatus by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById(R.id.uvc_video_status) as TextView?
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_mini_widget_solo_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        mUSBMonitor = USBMonitor(context, mOnDeviceConnectListener)

        textureView?.surfaceTextureListener = object : TextureView.SurfaceTextureListener{
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
                adjustAspectRatio(textureView as TextureView);
                surfaceRef = Surface(surface)
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                surfaceRef = null
                return true
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {

            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {

            }

        }
    }

    override fun onApiConnected() {
        //Toast.makeText(context, "onApiConnected", Toast.LENGTH_SHORT).show()
        broadcastManager.registerReceiver(receiver, MiniWidgetUVCLinkVideo.filter)
    }

    override fun onResume(){
        //Toast.makeText(context, "onResume", Toast.LENGTH_SHORT).show()
        super.onResume()
        mUSBMonitor?.register()
        mUVCCamera?.startPreview()
    }

    override fun onPause(){
        //Toast.makeText(context, "onPause", Toast.LENGTH_SHORT).show()
        super.onPause()
        mUSBMonitor?.unregister()
        mUVCCamera?.stopPreview()
    }

    override fun onDestroy() {
        //Toast.makeText(context, "onDestroy", Toast.LENGTH_SHORT).show()
        super.onDestroy()
        mUVCCamera?.destroy()
        mUSBMonitor?.destroy()
        isActive = false
        isPreview = false
    }

    override fun onApiDisconnected() {
        broadcastManager.unregisterReceiver(receiver)
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

    private val mOnDeviceConnectListener = object : USBMonitor.OnDeviceConnectListener {
        override fun onAttach(device: UsbDevice?) {
            //Toast.makeText(context, "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show()
        }

        override fun onConnect(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock, createNew: Boolean) {

            videoStatus?.visibility = View.GONE

            mUVCCamera?.destroy()
            isActive = false
            isPreview = false

            EXECUTER.execute(object : Runnable {
                override fun run() {
                    mUVCCamera = UVCCamera()
                    mUVCCamera?.open(ctrlBlock)
                    try {
                        mUVCCamera?.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.FRAME_FORMAT_MJPEG)
                    } catch (e: IllegalArgumentException) {
                        try {
                            mUVCCamera?.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.DEFAULT_PREVIEW_MODE)
                        } catch (e1: IllegalArgumentException) {
                            mUVCCamera?.destroy()
                            mUVCCamera = null
                        }

                    }
                    if ((mUVCCamera != null) && (surfaceRef != null)) {
                        isActive = true
                        mUVCCamera?.setPreviewDisplay(surfaceRef)
                        mUVCCamera?.startPreview()
                        isPreview = true
                    }
                }
            })
        }

        override fun onDisconnect(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock) {
            if (usbDevice?.equals(device)!!){
                //Toast.makeText(context, "onDisconnect", Toast.LENGTH_SHORT).show()
                mUVCCamera?.close()
                videoStatus?.visibility = View.VISIBLE
                isActive = false
                isPreview = false
            }
        }

        override fun onDettach(device: UsbDevice?) {
            if (usbDevice?.equals(device)!!){
                //Toast.makeText(context, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show()
                videoStatus?.visibility = View.VISIBLE
                isActive = false
                isPreview = false
            }
        }

        override fun onCancel() {
            Toast.makeText(context, "onCancel", Toast.LENGTH_SHORT).show()
            isActive = false
            isPreview = false
        }
    }


}