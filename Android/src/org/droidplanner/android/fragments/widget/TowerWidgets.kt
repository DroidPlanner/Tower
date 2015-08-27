package org.droidplanner.android.fragments.widget

import android.support.annotation.IdRes
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import org.droidplanner.android.R
import org.droidplanner.android.fragments.WidgetsListFragment

/**
 * Created by Fredia Huya-Kouadio on 8/25/15.
 */
public enum class TowerWidgets {
    TELEMETRY_INFO {
        override fun getLabelResId() = R.string.label_widget_telemetry_info

        override fun getIdRes() = R.id.tower_widget_telemetry_info

        override fun getMinimizedFragment() = MiniWidgetTelemetryInfo()
    },

    SOLO_VIDEO {
        override fun getLabelResId() = R.string.label_widget_solo_video

        override fun getIdRes() = R.id.tower_widget_solo_video

        override fun canMaximize() = true

        override fun getMinimizedFragment() = WidgetSoloLinkVideo()

        override fun getMaximizedFragment() = WidgetSoloLinkVideo()
    };

    @StringRes abstract fun getLabelResId(): Int

    @IdRes abstract fun getIdRes(): Int

    abstract fun getMinimizedFragment(): Fragment

    open fun canMaximize() = false

    open fun getMaximizedFragment(): Fragment? = null
}