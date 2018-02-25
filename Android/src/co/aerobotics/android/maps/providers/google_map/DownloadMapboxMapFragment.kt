package co.aerobotics.android.maps.providers.google_map

import android.widget.Toast

/**
 * Created by Fredia Huya-Kouadio on 6/17/15.
 */
class DownloadMapboxMapFragment : co.aerobotics.android.fragments.DroneMap() {
    override fun isMissionDraggable() = false

    override fun setAutoPanMode(target: co.aerobotics.android.utils.prefs.AutoPanMode?): Boolean {
        return when(target){
            co.aerobotics.android.utils.prefs.AutoPanMode.DISABLED -> true
            else -> {
                Toast.makeText(activity, "Auto pan is not supported on this map.", Toast.LENGTH_LONG).show()
                false
            }
        }
    }

    override fun shouldUpdateMission(): Boolean {
        return false
    }
}