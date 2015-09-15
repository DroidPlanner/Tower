package org.droidplanner.android.fragments.widget

import android.support.annotation.IdRes
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import org.droidplanner.android.R
import org.droidplanner.android.fragments.WidgetsListFragment
import org.droidplanner.android.fragments.widget.MiniWidgetSoloLinkVideo
import org.droidplanner.android.fragments.widget.diagnostics.FullWidgetDiagnostics
import kotlin.platform.platformStatic

/**
 * Created by Fredia Huya-Kouadio on 8/25/15.
 */
public enum class TowerWidgets(@IdRes val idRes: Int, @StringRes val labelResId: Int, @StringRes val descriptionResId: Int, val prefKey: String) {

    TELEMETRY_INFO(R.id.tower_widget_telemetry_info, R.string.label_widget_telemetry_info, R.string.description_widget_telemetry_info, "pref_widget_telemetry_info") {

        override fun getMinimizedFragment() = MiniWidgetTelemetryInfo()

        override fun isEnabledByDefault() = true
    },

    SOLO_VIDEO(R.id.tower_widget_solo_video, R.string.label_widget_solo_video, R.string.description_widget_solo_video, "pref_widget_solo_video") {

        override fun canMaximize() = true

        override fun isEnabledByDefault() = true

        override fun getMinimizedFragment() = MiniWidgetSoloLinkVideo()

        override fun getMaximizedFragment() = FullWidgetSoloLinkVideo()
    },

    VEHICLE_DIAGNOSTICS(R.id.tower_widget_vehicle_diagnostics, R.string.label_widget_vehicle_diagnostics, R.string.description_widget_vehicle_diagnostics, "pref_widget_vehicle_diagnostics"){
        override fun getMinimizedFragment() = MiniWidgetDiagnostics()

        override fun canMaximize() = true

        override fun getMaximizedFragment() = FullWidgetDiagnostics()
    };

    abstract fun getMinimizedFragment(): TowerWidget

    open fun canMaximize() = false

    open fun isEnabledByDefault() = false

    open fun getMaximizedFragment(): TowerWidget? = null

    companion object {
        @platformStatic fun getWidgetById(@IdRes id: Int): TowerWidgets? {
            return when(id){
                TELEMETRY_INFO.idRes -> TELEMETRY_INFO
                SOLO_VIDEO.idRes -> SOLO_VIDEO
                VEHICLE_DIAGNOSTICS.idRes -> VEHICLE_DIAGNOSTICS
                else -> null
            }
        }

        @platformStatic fun getWidgetByPrefKey(prefKey: String): TowerWidgets?{
            return when(prefKey){
                TELEMETRY_INFO.prefKey -> TELEMETRY_INFO
                SOLO_VIDEO.prefKey -> SOLO_VIDEO
                VEHICLE_DIAGNOSTICS.prefKey -> VEHICLE_DIAGNOSTICS
                else -> null
            }
        }
    }
}