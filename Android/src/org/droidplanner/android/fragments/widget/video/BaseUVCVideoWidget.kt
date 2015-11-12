package org.droidplanner.android.fragments.widget.video

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.hardware.usb.UsbDevice
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.TextView
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent
import com.serenegiant.usb.USBMonitor
import com.serenegiant.usb.UVCCamera
import org.droidplanner.android.R
import org.droidplanner.android.dialogs.UVCDialog
import org.droidplanner.android.fragments.widget.TowerWidget
import org.droidplanner.android.fragments.widget.TowerWidgets
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


abstract class BaseUVCVideoWidget : TowerWidget() , USBMonitor.OnDeviceConnectListener{

    companion object {

        private val filter = initFilter()

        private fun initFilter(): IntentFilter {
            val temp = IntentFilter()
            temp.addAction(AttributeEvent.STATE_CONNECTED)
            temp.addAction(AttributeEvent.STATE_ARMING)
            return temp
        }
    }

    override fun getWidgetType() = TowerWidgets.UVC_VIDEO

    protected var surfaceRef: Surface? = null

    // for thread pool
    protected val CORE_POOL_SIZE = 1   // initial/minimum threads
    protected val MAX_POOL_SIZE = 4    // maximum threads
    protected val KEEP_ALIVE_TIME = 10 // time periods while keep the idle thread
    protected val EXECUTER: ThreadPoolExecutor = ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME.toLong(),
            TimeUnit.SECONDS, LinkedBlockingQueue<Runnable>())

    // for accessing USB and USB camera
    protected var mUSBMonitor: USBMonitor? = null
    protected var mUVCCamera: UVCCamera? = null
    protected var isActive: Boolean = false
    protected var isPreview:Boolean = false
    protected var usbDevice: UsbDevice? = null

    protected val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                AttributeEvent.STATE_CONNECTED -> {
                    if (!isActive && !isPreview){
                        UVCDialog.showDialog(activity,mUSBMonitor);
                    }
                }
                AttributeEvent.STATE_ARMING -> {
                    if (!isActive && !isPreview){

                    }
                }
            }
        }
    }

    protected val textureView by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById(R.id.uvc_video_view) as TextureView?
    }

    protected val videoStatus by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById(R.id.uvc_video_status) as TextView?
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        mUSBMonitor = USBMonitor(context, this)

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
        broadcastManager.registerReceiver(receiver, filter)
    }

    override fun onResume(){
        super.onResume()
        mUSBMonitor?.register()
        mUVCCamera?.startPreview()
    }

    override fun onPause(){
        super.onPause()
        mUSBMonitor?.unregister()
        mUVCCamera?.stopPreview()
    }

    override fun onDestroy() {
        super.onDestroy()
        mUVCCamera?.destroy()
        mUSBMonitor?.destroy()
        isActive = false
        isPreview = false
    }

    override fun onApiDisconnected() {
        broadcastManager.unregisterReceiver(receiver)
    }


    override fun onAttach(device: UsbDevice?) {

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
            mUVCCamera?.close()
            isActive = false
            isPreview = false
            videoStatus?.visibility = View.VISIBLE
        }
    }

    override fun onDettach(device: UsbDevice?) {
        if (usbDevice?.equals(device)!!){
            isActive = false
            isPreview = false
            videoStatus?.visibility = View.VISIBLE
        }
    }

    override fun onCancel() {
        isActive = false
        isPreview = false
        videoStatus?.visibility = View.VISIBLE
    }

    protected fun adjustAspectRatio(textureView: TextureView) {
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