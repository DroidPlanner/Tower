package org.droidplanner.android.tlogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.MAVLink.common.msg_global_position_int
import com.o3dr.android.client.utils.data.tlog.TLogParser
import org.droidplanner.android.R
import java.util.*

/**
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
class TLogPositionViewer : TLogDataSubscriber() {

    private val positionEvents = ArrayList<msg_global_position_int>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        return inflater.inflate(R.layout.fragment_tlog_position_viewer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        val fm = childFragmentManager
        var tlogEventMap = fm.findFragmentById(R.id.tlog_map_container) as TLogEventMapFragment?
        if(tlogEventMap == null){
            tlogEventMap = TLogEventMapFragment()
            fm.beginTransaction().add(R.id.tlog_map_container, tlogEventMap).commit()
        }
    }

    override fun onTLogDataLoaded(events: List<TLogParser.Event>) {
        // Parse the event list and retrieve only the position events.
        positionEvents.clear()
        for(event in events){
            if(event.mavLinkMessage is msg_global_position_int){
                positionEvents.add(event.mavLinkMessage as msg_global_position_int)
            }
        }

        // Refresh the adapter

    }
}