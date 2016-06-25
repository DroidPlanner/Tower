package org.droidplanner.android.tlog.event

import android.widget.Toast
import com.o3dr.android.client.utils.data.tlog.TLogParser
import org.droidplanner.android.fragments.DroneMap
import org.droidplanner.android.tlog.interfaces.TLogDataSubscriber
import org.droidplanner.android.utils.prefs.AutoPanMode

/**
 * TODO: Add polylines to the map to represent the tlog position events trail
 * TODO: add marker to the map for when a single tlog position event is selected
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
class TLogEventMapFragment : DroneMap(), TLogDataSubscriber {

    override fun isMissionDraggable() = false

    override fun setAutoPanMode(target: AutoPanMode?): Boolean {
        return when(target){
            AutoPanMode.DISABLED -> true
            else -> {
                Toast.makeText(activity, "Auto pan is not supported on this map.",
                        Toast.LENGTH_LONG).show()
                false
            }
        }
    }

    override fun onTLogDataLoaded(events: List<TLogParser.Event>) {

    }
}