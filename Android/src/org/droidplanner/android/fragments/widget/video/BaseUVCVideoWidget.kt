package org.droidplanner.android.fragments.widget.video

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.SurfaceTexture
import android.hardware.usb.UsbDevice
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent
import com.o3dr.services.android.lib.drone.attribute.AttributeType
import com.o3dr.services.android.lib.drone.property.State
import com.serenegiant.usb.DeviceFilter
import com.serenegiant.usb.USBMonitor
import com.serenegiant.usb.UVCCamera
import org.droidplanner.android.R
import org.droidplanner.android.dialogs.UVCDialog
import org.droidplanner.android.fragments.widget.TowerWidget
import org.droidplanner.android.fragments.widget.TowerWidgets
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import android.graphics.Matrix
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs

abstract class BaseUVCVideoWidget : TowerWidget(){

    companion object {

        private val filter = initFilter()

        private fun initFilter(): IntentFilter {
            val temp = IntentFilter()
            temp.addAction(AttributeEvent.STATE_CONNECTED)
            return temp
        }

    }

    protected val DEBUG = false

    protected val TAG = "BaseUVCVideoWidget"

    override fun getWidgetType() = TowerWidgets.UVC_VIDEO

    // for thread pool
    protected val CORE_POOL_SIZE = 1   // initial/minimum threads
    protected val MAX_POOL_SIZE = 4    // maximum threads
    protected val KEEP_ALIVE_TIME = 10 // time periods while keep the idle thread
    protected val EXECUTER: ThreadPoolExecutor = ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME.toLong(),
            TimeUnit.SECONDS, LinkedBlockingQueue<Runnable>())

    //Aspect ratio
    protected val ASPECT_RATIO_4_3: Float = 3f / 4f
    protected val ASPECT_RATIO_16_9: Float = 9f / 16f
    protected val ASPECT_RATIO_21_9: Float = 9f / 21f
    protected val ASPECT_RATIO_1_1: Float = 1f / 1f
    protected var aspectRatio: Float = ASPECT_RATIO_4_3

    // for accessing USB and USB camera
    protected var mUSBMonitor: USBMonitor? = null
    protected var mUVCCamera: UVCCamera? = null
    protected var isPreview:Boolean = false
    protected var usbDevice: UsbDevice? = null
    protected var mPreviewSurface: Surface? = null


    protected val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                AttributeEvent.STATE_CONNECTED -> {
                    startVideoStreaming()
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

    override fun onApiConnected() {
        if (DEBUG) Log.v(TAG, "onApiConnected:")

        broadcastManager.registerReceiver(receiver, filter)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        textureView?.setSurfaceTextureListener(mSurfaceTextureListener)

        mUSBMonitor = USBMonitor(activity, mOnDeviceConnectListener)

        if (DEBUG) Log.v(TAG, "onViewCreated:")

    }

    override fun onResume() {
        super.onResume()
        if (DEBUG) Log.v(TAG, "onResume:")

        mUSBMonitor?.register()
        aspectRatio = appPrefs.uvcVideoAspectRatio
    }

    override fun onPause() {
        super.onPause()
        if (DEBUG) Log.v(TAG, "onPause:")

        mUSBMonitor?.unregister()
        mUVCCamera?.close()
        appPrefs.uvcVideoAspectRatio = aspectRatio
    }

    override fun onDestroy() {
        super.onDestroy()
        if (DEBUG) Log.v(TAG, "onDestroy:")

        mUVCCamera?.destroy()
        mUVCCamera = null
        isPreview = false
        mUSBMonitor?.destroy()
        mUSBMonitor = null
    }

    override fun onApiDisconnected() {
        broadcastManager.unregisterReceiver(receiver)
        if (DEBUG) Log.v(TAG, "onApiDisconnected:")
    }


    private val mOnDeviceConnectListener = object : USBMonitor.OnDeviceConnectListener {
        override fun onAttach(device: UsbDevice?) {
            if (DEBUG) Log.v(TAG, "onAttach:")
            startVideoStreaming()
        }

        override fun onConnect(device: UsbDevice, ctrlBlock: USBMonitor.UsbControlBlock, createNew: Boolean) {
            if (DEBUG) Log.v(TAG, "onConnect:")

            usbDevice = device
            mUVCCamera?.destroy()
            mUVCCamera = UVCCamera()

            videoStatus?.visibility = View.GONE

            EXECUTER.execute(object : Runnable {
                override fun run() {
                    mUVCCamera?.open(ctrlBlock)

                    if (DEBUG) Log.i(TAG, "supportedSize:" + mUVCCamera?.getSupportedSize())

                    mPreviewSurface?.release()
                    mPreviewSurface = null

                    try {
                        mUVCCamera?.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.FRAME_FORMAT_MJPEG)
                    } catch (e: IllegalArgumentException) {
                        try {
                            // fallback to YUV mode
                            mUVCCamera?.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.DEFAULT_PREVIEW_MODE)
                        } catch (e1: IllegalArgumentException) {
                            mUVCCamera?.destroy()
                            mUVCCamera = null
                        }
                    }

                    if (mUVCCamera != null) {
                        val st = textureView?.getSurfaceTexture()
                        if (st != null) {
                            mPreviewSurface = Surface(st)
                            mUVCCamera?.setPreviewDisplay(mPreviewSurface)
                            mUVCCamera?.startPreview()
                            isPreview = true
                        }

                    }
                }
            })
        }

        override fun onDisconnect(device: UsbDevice, ctrlBlock: USBMonitor.UsbControlBlock) {
            if (DEBUG) Log.v(TAG, "onDisconnect:")

            mUVCCamera?.close()
            mPreviewSurface?.release()
            mPreviewSurface = null
            isPreview = false

            videoStatus?.visibility = View.VISIBLE
        }

        override fun onDettach(device: UsbDevice) {
            if (DEBUG) Log.v(TAG, "onDettach:")

            mUVCCamera?.close()
            mPreviewSurface?.release()
            mPreviewSurface = null
            isPreview = false

            videoStatus?.visibility = View.VISIBLE
        }

        override fun onCancel() {
            videoStatus?.visibility = View.VISIBLE
        }
    }

    protected fun startVideoStreaming(){
        if (DEBUG) Log.v(TAG, "startVideoStreaming:")

        if (usbDevice != null) {
            mUSBMonitor?.requestPermission(usbDevice);
        } else {
            //UVC Device Filter
            val uvcFilter = DeviceFilter.getDeviceFilters(activity, R.xml.uvc_device_filter)
            val uvcDevices = mUSBMonitor?.getDeviceList(uvcFilter[0])
            if (uvcDevices == null || uvcDevices.isEmpty()) {
                if (DEBUG) Log.v(TAG, getString(R.string.uvc_device_no_device))
            } else {
                if (uvcDevices.size.compareTo(1) == 0) {
                    usbDevice = uvcDevices.get(0);
                    mUSBMonitor?.requestPermission(usbDevice)
                } else {
                    UVCDialog.showDialog(activity, mUSBMonitor)
                }
            }
        }
    }

    private val mSurfaceTextureListener = object : TextureView.SurfaceTextureListener {

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            adjustAspectRatio(textureView as TextureView)
            startVideoStreaming()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            mPreviewSurface?.release()
            mPreviewSurface = null
            return true
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

        }
    }

    protected fun adjustAspectRatio(textureView: TextureView) {
        val viewWidth = textureView.width
        val viewHeight = textureView.height

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