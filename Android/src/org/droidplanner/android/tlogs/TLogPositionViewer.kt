package org.droidplanner.android.tlogs

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.MAVLink.common.msg_global_position_int
import com.o3dr.android.client.utils.data.tlog.TLogParser
import org.droidplanner.android.R
import org.droidplanner.android.tlogs.adapter.TLogPositionEventAdapter
import java.util.*

/**
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
class TLogPositionViewer : TLogDataSubscriber() {

    private val tlogPositionAdapter = TLogPositionEventAdapter()

    private val positionEvents = ArrayList<TLogParser.Event>()

    private val eventsView by lazy {
        getView()?.findViewById(R.id.event_list) as RecyclerView?
    }

    private val jumpToBeginning by lazy {
        getView()?.findViewById(R.id.jump_to_beginning)
    }

    private val jumpToEnd by lazy {
        getView()?.findViewById(R.id.jump_to_end)
    }

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

        eventsView?.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = tlogPositionAdapter
        }
        jumpToBeginning?.setOnClickListener {
            // Jump to the beginning of the list
            eventsView?.scrollToPosition(0)
        }

        jumpToEnd?.setOnClickListener {
            // Jump to the end of the list
            eventsView?.scrollToPosition(tlogPositionAdapter.itemCount -1)
        }
    }

    override fun onTLogDataLoaded(events: List<TLogParser.Event>) {
        // Parse the event list and retrieve only the position events.
        positionEvents.clear()
        positionEvents.addAll( events.filter { event -> event.mavLinkMessage is msg_global_position_int })

        // Refresh the adapter
        tlogPositionAdapter.loadTLogPositionEvents(positionEvents)

        val twoOrMore = tlogPositionAdapter.itemCount > 1
        jumpToBeginning?.isEnabled = twoOrMore
        jumpToEnd?.isEnabled = twoOrMore
    }
}