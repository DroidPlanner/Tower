package org.droidplanner.android.tlogs

import android.widget.Toast
import org.droidplanner.android.fragments.DroneMap
import org.droidplanner.android.utils.prefs.AutoPanMode

/**
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
class TLogEventMapFragment : DroneMap() {
    override fun isMissionDraggable() = false

    override fun setAutoPanMode(target: AutoPanMode?): Boolean {
        return when(target){
            AutoPanMode.DISABLED -> true
            else -> {
                Toast.makeText(activity, "Auto pan is not supported on this map.", Toast.LENGTH_LONG).show()
                false
            }
        }
    }
}