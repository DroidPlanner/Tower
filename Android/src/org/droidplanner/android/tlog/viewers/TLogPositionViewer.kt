package org.droidplanner.android.tlog.viewers

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.MAVLink.common.msg_global_position_int
import com.o3dr.android.client.utils.data.tlog.TLogParser
import org.droidplanner.android.R
import org.droidplanner.android.tlog.adapters.TLogPositionEventAdapter
import org.droidplanner.android.tlog.event.TLogEventDetail
import org.droidplanner.android.tlog.event.TLogEventListener
import org.droidplanner.android.tlog.event.TLogEventMapFragment
import org.droidplanner.android.view.FastScroller
import java.util.*

/**
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
class TLogPositionViewer : TLogViewer(), TLogEventListener {

    private val tlogPositionAdapter = TLogPositionEventAdapter()

    private val positionEvents = ArrayList<TLogParser.Event>()

    private val noDataView by lazy {
        getView()?.findViewById(R.id.no_data_message)
    }

    private val eventsView by lazy {
        getView()?.findViewById(R.id.event_list) as RecyclerView?
    }

    private val fastScroller by lazy {
        getView()?.findViewById(R.id.fast_scroller) as FastScroller
    }

    private var tlogEventMap : TLogEventMapFragment? = null
    private var tlogEventDetail : TLogEventDetail? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        return inflater.inflate(R.layout.fragment_tlog_position_viewer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        val fm = childFragmentManager
        tlogEventMap = fm.findFragmentById(R.id.tlog_map_container) as TLogEventMapFragment?
        if(tlogEventMap == null){
            tlogEventMap = TLogEventMapFragment()
            fm.beginTransaction().add(R.id.tlog_map_container, tlogEventMap).commit()
        }

        tlogEventDetail = fm.findFragmentById(R.id.tlog_event_detail) as TLogEventDetail?
        if(tlogEventDetail == null){
            tlogEventDetail = TLogEventDetail()
            fm.beginTransaction().add(R.id.tlog_event_detail, tlogEventDetail).commit()
        }

        eventsView?.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = tlogPositionAdapter
        }
        fastScroller.setRecyclerView(eventsView!!)
        tlogPositionAdapter.setTLogEventClickListener(this)

        val goToMyLocation = view.findViewById(R.id.my_location_button) as FloatingActionButton
        goToMyLocation.setOnClickListener {
            tlogEventMap?.goToMyLocation();
        }

        val goToDroneLocation = view.findViewById(R.id.drone_location_button) as FloatingActionButton
        goToDroneLocation.setOnClickListener {
            tlogEventMap?.goToDroneLocation()
        }
    }

    override fun onTLogDataLoaded(events: List<TLogParser.Event>) {
        // Parse the event list and retrieve only the position events.
        positionEvents.clear()
        var lastEventTimestamp = -1L
        for(event in events){
            if(event.mavLinkMessage is msg_global_position_int) {
                // Events should be at least 1 second apart.
                if(lastEventTimestamp == -1L || (event.timestamp/1000 - lastEventTimestamp/1000) >= 1L){
                    lastEventTimestamp = event.timestamp
                    positionEvents.add(event)
                }
            }
        }

        // Refresh the adapter
        tlogPositionAdapter.loadTLogPositionEvents(positionEvents)

        // Refresh the map.
        tlogEventMap?.onTLogEventSelected(null)
        tlogEventMap?.onTLogDataLoaded(positionEvents)

        // Refresh the event detail window
        tlogEventDetail?.onTLogEventSelected(null)

        if(positionEvents.isEmpty()){
            stateNoData()
        } else {
            stateDataLoaded()
        }
    }

    override fun onTLogEventSelected(event: TLogParser.Event?) {
        // Show the detail window for this event
        tlogEventDetail?.onTLogEventSelected(event)

        //Propagate the click event to the map
        tlogEventMap?.onTLogEventSelected(event)
    }

    private fun stateNoData(){
        noDataView?.visibility = View.VISIBLE
        eventsView?.visibility = View.GONE
        fastScroller.visibility = View.GONE
    }

    private fun stateDataLoaded(){
        noDataView?.visibility = View.GONE
        eventsView?.visibility = View.VISIBLE
        fastScroller.visibility = View.VISIBLE
    }
}

