package org.droidplanner.android.fragments.widget

import android.support.annotation.IdRes
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import org.droidplanner.android.R
import org.droidplanner.android.fragments.WidgetsListFragment
import kotlin.platform.platformStatic

/**
 * Created by Fredia Huya-Kouadio on 8/25/15.
 */
public enum class TowerWidgets(@IdRes val idRes: Int, @StringRes val labelResId: Int) {

    TELEMETRY_INFO(R.id.tower_widget_telemetry_info, R.string.label_widget_telemetry_info) {

        override fun getMinimizedFragment() = MiniWidgetTelemetryInfo()
    },

    SOLO_VIDEO(R.id.tower_widget_solo_video, R.string.label_widget_solo_video) {

        override fun canMaximize() = true

        override fun getMinimizedFragment() = WidgetSoloLinkVideo()

        override fun getMaximizedFragment() = WidgetSoloLinkVideo()
    };

    abstract fun getMinimizedFragment(): TowerWidget

    open fun canMaximize() = false

    open fun getMaximizedFragment(): TowerWidget? = null

    companion object {
        @platformStatic fun getWidgetById(@IdRes id: Int): TowerWidgets? {
            return when(id){
                TELEMETRY_INFO.idRes -> TELEMETRY_INFO
                SOLO_VIDEO.idRes -> SOLO_VIDEO
                else -> null
            }
        }
    }
}