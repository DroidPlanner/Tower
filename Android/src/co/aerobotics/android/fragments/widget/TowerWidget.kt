package co.aerobotics.android.fragments.widget

/**
 * Created by Fredia Huya-Kouadio on 8/28/15.
 */
abstract class TowerWidget : co.aerobotics.android.fragments.helpers.ApiListenerFragment() {

    abstract fun getWidgetType(): TowerWidgets
}