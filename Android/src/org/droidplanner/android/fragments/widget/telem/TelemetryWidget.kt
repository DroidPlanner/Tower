package org.droidplanner.android.fragments.widget.telem

import android.support.annotation.IdRes
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import org.droidplanner.android.R
import org.droidplanner.android.fragments.TelemetryFragment

/**
 * Created by Fredia Huya-Kouadio on 8/25/15.
 */
public enum class TelemetryWidget {
    SOLO_VIDEO {
        override fun getLabelResId() = R.string.label_widget_solo_video

        override fun getIdRes() = R.id.telem_widget_solo_video

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