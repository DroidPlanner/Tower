package co.aerobotics.android.fragments.widget

import android.app.DialogFragment
import android.support.annotation.IdRes
import android.support.annotation.StringRes
import co.aerobotics.android.fragments.widget.diagnostics.FullWidgetDiagnostics
import co.aerobotics.android.fragments.widget.diagnostics.MiniWidgetDiagnostics
import co.aerobotics.android.fragments.widget.telemetry.MiniWidgetAttitudeSpeedInfo
import co.aerobotics.android.fragments.widget.telemetry.MiniWidgetFlightTimer
import co.aerobotics.android.fragments.widget.telemetry.MiniWidgetGeoInfo
import co.aerobotics.android.fragments.widget.video.*
import co.aerobotics.android.fragments.widget.weather.MiniWidgetWeatherInfo

/**
 * Created by Fredia Huya-Kouadio on 8/25/15.
 */
enum class TowerWidgets(@IdRes val idRes: Int, @StringRes val labelResId: Int, @StringRes val descriptionResId: Int, val prefKey: String) {

    FLIGHT_TIMER(co.aerobotics.android.R.id.tower_widget_flight_timer, co.aerobotics.android.R.string.label_widget_flight_timer, co.aerobotics.android.R.string.description_widget_flight_timer, "pref_widget_flight_timer"){
        override fun getMinimizedFragment() = MiniWidgetFlightTimer()

        override fun internalIsVisibleByDefault() = true
    },

    VEHICLE_DIAGNOSTICS(co.aerobotics.android.R.id.tower_widget_vehicle_diagnostics, co.aerobotics.android.R.string.label_widget_vehicle_diagnostics, co.aerobotics.android.R.string.description_widget_vehicle_diagnostics, "pref_widget_vehicle_diagnostics") {
        override fun getMinimizedFragment() = MiniWidgetDiagnostics()

        override fun canMaximize() = true

        override fun getMaximizedFragment() = FullWidgetDiagnostics()
    },

    SOLO_VIDEO(co.aerobotics.android.R.id.tower_widget_solo_video, co.aerobotics.android.R.string.label_widget_solo_video, co.aerobotics.android.R.string.description_widget_solo_video, "pref_widget_solo_video") {

        override fun canMaximize() = true

        override fun internalIsVisibleByDefault() = true

        override fun getMinimizedFragment() = MiniWidgetSoloLinkVideo()

        override fun getMaximizedFragment() = FullWidgetSoloLinkVideo()

        override fun hasPreferences() = true

        override fun getPrefFragment() = WidgetVideoPreferences()
    },

    UVC_VIDEO(co.aerobotics.android.R.id.tower_widget_uvc_video, co.aerobotics.android.R.string.label_widget_uvc_video, co.aerobotics.android.R.string.description_widget_uvc_video, "pref_widget_uvc_video") {

        override fun canMaximize() = true

        override fun internalIsVisibleByDefault() = false

        override fun getMinimizedFragment() = MiniWidgetUVCLinkVideo()

        override fun getMaximizedFragment() = FullWidgetUVCLinkVideo()
    },


    ATTITUDE_SPEED_INFO(co.aerobotics.android.R.id.tower_widget_attitude_speed_info, co.aerobotics.android.R.string.label_widget_attitude_speed_info, co.aerobotics.android.R.string.description_widget_attitude_speed_info, "pref_widget_attitude_speed_info") {

        override fun getMinimizedFragment() = MiniWidgetAttitudeSpeedInfo()

        override fun internalIsVisibleByDefault() = true
    },

    GEO_INFO(co.aerobotics.android.R.id.tower_widget_geo_info, co.aerobotics.android.R.string.label_widget_geo_info, co.aerobotics.android.R.string.description_widget_geo_info, "pref_widget_geo_info"){
        override fun getMinimizedFragment() = MiniWidgetGeoInfo()
    },

    //Weather widget
    WEATHER_INFO(co.aerobotics.android.R.id.tower_widget_weather_info, co.aerobotics.android.R.string.label_widget_weather_info, co.aerobotics.android.R.string.description_widget_weather_info, "pref_widget_weather_info"){
        override fun getMinimizedFragment() = MiniWidgetWeatherInfo()

        //TODO: enable for version 4.1.0
        override fun isEnabled() = false
    }
    ;

    abstract fun getMinimizedFragment(): TowerWidget

    open fun canMaximize() = false

    fun isVisibleByDefault() = isEnabled() && internalIsVisibleByDefault()

    protected open fun internalIsVisibleByDefault() = false

    protected open fun isEnabled() = true

    open fun getMaximizedFragment(): TowerWidget? = null

    open fun hasPreferences() = false

    open fun getPrefFragment(): DialogFragment? = null

    companion object {
        @JvmStatic fun getWidgetById(@IdRes id: Int): TowerWidgets? {
            return when (id) {
                FLIGHT_TIMER.idRes -> FLIGHT_TIMER
                ATTITUDE_SPEED_INFO.idRes -> ATTITUDE_SPEED_INFO
                SOLO_VIDEO.idRes -> SOLO_VIDEO
                UVC_VIDEO.idRes -> UVC_VIDEO
                VEHICLE_DIAGNOSTICS.idRes -> VEHICLE_DIAGNOSTICS
                GEO_INFO.idRes -> GEO_INFO
                WEATHER_INFO.idRes -> WEATHER_INFO
                else -> null
            }
        }

        @JvmStatic fun getWidgetByPrefKey(prefKey: String): TowerWidgets? {
            return when (prefKey) {
                FLIGHT_TIMER.prefKey -> FLIGHT_TIMER
                ATTITUDE_SPEED_INFO.prefKey -> ATTITUDE_SPEED_INFO
                SOLO_VIDEO.prefKey -> SOLO_VIDEO
                UVC_VIDEO.prefKey -> UVC_VIDEO
                VEHICLE_DIAGNOSTICS.prefKey -> VEHICLE_DIAGNOSTICS
                GEO_INFO.prefKey -> GEO_INFO
                WEATHER_INFO.prefKey -> WEATHER_INFO
                else -> null
            }
        }

        @JvmStatic fun enabledWidgets() = TowerWidgets.values().filter { widget -> widget.isEnabled() }
    }
}