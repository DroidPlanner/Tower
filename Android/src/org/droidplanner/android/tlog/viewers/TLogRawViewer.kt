package org.droidplanner.android.tlog.viewers

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.o3dr.android.client.utils.data.tlog.TLogParser
import org.droidplanner.android.R
import org.droidplanner.android.droneshare.data.SessionContract
import org.droidplanner.android.tlog.adapters.TLogRawEventAdapter
import org.droidplanner.android.view.FastScroller

/**
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
class TLogRawViewer : TLogViewer() {

    private var tlogEventsAdapter : TLogRawEventAdapter? = null

    private val loadingData by lazy {
        getView()?.findViewById(R.id.loading_tlog_data)
    }

    private val noTLogView by lazy {
        getView()?.findViewById(R.id.no_tlog_selected) as TextView?
    }

    private val fastScroller by lazy {
        getView()?.findViewById(R.id.raw_fastscroller) as FastScroller
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
        }

        tlogEventsAdapter = TLogRawEventAdapter(rawData!!, null)
        rawData?.adapter = tlogEventsAdapter

        fastScroller.setRecyclerView(rawData!!)
    }

    override fun onClearTLogData() {
        tlogEventsAdapter?.clear()
        stateNoData()
    }

    override fun onTLogSelected(tlogSession: SessionContract.SessionData){
        tlogEventsAdapter?.clear()
        stateLoadingData()
    }

    override fun onTLogDataLoaded(events: List<TLogParser.Event>, hasMore: Boolean){
        // Refresh the recycler view
        tlogEventsAdapter?.addItems(events)
        tlogEventsAdapter?.setHasMoreData(hasMore)

        if(tlogEventsAdapter?.itemCount == 0){
            if(hasMore){
                stateLoadingData()
            }
            else {
                stateNoData()
            }
        }
        else{
            stateDataLoaded()
        }
    }

    private fun stateLoadingData(){
        noTLogView?.visibility = View.GONE
        rawData?.visibility = View.GONE
        fastScroller.visibility = View.GONE
        loadingData?.visibility = View.VISIBLE
    }

    private fun stateNoData(){
        noTLogView?.visibility = View.VISIBLE
        rawData?.visibility = View.GONE
        fastScroller.visibility = View.GONE
        loadingData?.visibility = View.GONE
    }

    private fun stateDataLoaded(){
        noTLogView?.visibility = View.GONE
        loadingData?.visibility = View.GONE
        rawData?.visibility = View.VISIBLE
        fastScroller.visibility = View.VISIBLE
    }
}