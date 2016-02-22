package org.droidplanner.android.fragments.widget.video

import android.os.Bundle
import android.view.Surface
import com.o3dr.android.client.apis.CameraApi
import com.o3dr.android.client.apis.solo.SoloCameraApi
import com.o3dr.services.android.lib.model.AbstractCommandListener
import org.droidplanner.android.fragments.widget.TowerWidget
import org.droidplanner.android.fragments.widget.TowerWidgets
import timber.log.Timber

/**
 * Created by Fredia Huya-Kouadio on 10/18/15.
 */
abstract class BaseVideoWidget : TowerWidget() {

    override fun getWidgetType() = TowerWidgets.SOLO_VIDEO

    protected fun startVideoStream(surface: Surface, tag: String, listener: AbstractCommandListener?) {
        val appPrefs = appPrefs
        val videoType = appPrefs.videoWidgetType
        when (videoType) {
            WidgetVideoPreferences.SOLO_VIDEO_TYPE -> {
                Timber.d("Starting video stream with tag %s", tag)
                SoloCameraApi.getApi(drone).startVideoStream(surface, tag, listener)
            }

            WidgetVideoPreferences.CUSTOM_VIDEO_TYPE -> {
                val udpPort = appPrefs.customVideoUdpPort
                val bundle = Bundle()
                bundle.putInt(CameraApi.VIDEO_PROPS_UDP_PORT, udpPort)

                Timber.d("Starting video stream with tag %s from udp port %d", tag, udpPort)
                CameraApi.getApi(drone).startVideoStream(surface, tag, bundle, listener)
            }
        }
    }

    protected fun stopVideoStream(tag: String, listener: AbstractCommandListener?) {
        val appPrefs = appPrefs
        val videoType = appPrefs.videoWidgetType
        when (videoType) {
            WidgetVideoPreferences.SOLO_VIDEO_TYPE -> {
                Timber.d("Stopping video stream with tag %s", tag)
                SoloCameraApi.getApi(drone).stopVideoStream(tag, listener)
            }

            WidgetVideoPreferences.CUSTOM_VIDEO_TYPE -> {
                Timber.d("Stopping video stream with tag %s", tag)
                CameraApi.getApi(drone).stopVideoStream(tag, listener)
            }
        }


    }
}