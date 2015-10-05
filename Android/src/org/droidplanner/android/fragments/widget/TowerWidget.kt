package org.droidplanner.android.fragments.widget

import android.support.annotation.IdRes
import org.droidplanner.android.fragments.helpers.ApiListenerFragment

/**
 * Created by Fredia Huya-Kouadio on 8/28/15.
 */
public abstract class TowerWidget : ApiListenerFragment() {

    abstract fun getWidgetType(): TowerWidgets
}