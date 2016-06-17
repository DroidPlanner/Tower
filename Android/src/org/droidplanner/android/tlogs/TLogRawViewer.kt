package org.droidplanner.android.tlogs

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.o3dr.android.client.utils.data.tlog.TLogParser
import org.droidplanner.android.R
import org.droidplanner.android.tlogs.adapter.TLogRawEventAdapter

/**
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
class TLogRawViewer : TLogDataSubscriber() {

    private val tlogEventsAdapter = TLogRawEventAdapter()

    private val noTLogView by lazy {
        getView()?.findViewById(R.id.no_tlog_selected) as TextView?
    }

    private val rawData by lazy {
        getView()?.findViewById(R.id.tlog_raw_data) as RecyclerView?
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tlog_raw_viewer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rawData?.apply{
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(getContext())
            adapter = tlogEventsAdapter
        }
    }

    override fun onTLogDataLoaded(events: List<TLogParser.Event>){
        // Refresh the recycler view
        tlogEventsAdapter.loadTLogEvents(events)
        if(events.isEmpty()){
            stateNoData()
        }
        else{
            stateDataLoaded()
        }
    }

    private fun stateNoData(){
        noTLogView?.visibility = View.VISIBLE
        rawData?.visibility = View.GONE
    }

    private fun stateDataLoaded(){
        noTLogView?.visibility = View.GONE
        rawData?.visibility = View.VISIBLE
    }
}