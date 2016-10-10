package org.droidplanner.android.tlog.viewers

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.Toast
import com.MAVLink.common.msg_global_position_int
import com.o3dr.android.client.utils.data.tlog.TLogParser
import org.droidplanner.android.R
import org.droidplanner.android.activities.EditorActivity
import org.droidplanner.android.droneshare.data.SessionContract
import org.droidplanner.android.tlog.adapters.TLogPositionEventAdapter
import org.droidplanner.android.tlog.event.TLogEventListener
import org.droidplanner.android.tlog.event.TLogEventMapFragment
import org.droidplanner.android.utils.MapUtils
import org.droidplanner.android.utils.SpaceTime
import org.droidplanner.android.view.FastScroller

/**
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
class TLogPositionViewer : TLogViewer(), TLogEventListener {

    companion object {
        fun tlogEventToSpaceTime(event: TLogParser.Event): SpaceTime? {
            if(event.mavLinkMessage !is msg_global_position_int)
                return null

            val position = event.mavLinkMessage as msg_global_position_int
            return SpaceTime(position.lat.toDouble()/ 1E7,
                    position.lon.toDouble()/ 1E7,
                    getEventAltitude(position),
                    event.timestamp)
        }

        fun getEventAltitude(position: msg_global_position_int) : Double {
            return (position.relative_alt / 1000.0)
        }

        const val STATE_NO_DATA = 0
        const val STATE_LOADING_DATA = 1
        const val STATE_DATA_LOADED = 2
    }

    private var tlogPositionAdapter : TLogPositionEventAdapter? = null

    private val noDataView by lazy {
        getView()?.findViewById(R.id.no_data_message)
    }

    private val loadingData by lazy {
        getView()?.findViewById(R.id.loading_tlog_data)
    }

    private val eventsView by lazy {
        getView()?.findViewById(R.id.event_list) as RecyclerView?
    }

    private val fastScroller by lazy {
        getView()?.findViewById(R.id.fast_scroller) as FastScroller
    }

    private val newPositionEvents = mutableListOf<TLogParser.Event>()

    private var tlogEventMap : TLogEventMapFragment? = null

    private var lastEventTimestamp = -1L
    private var toleranceInPixels = 0.0

    private var currentState = STATE_NO_DATA
    private var missionExportMenuItem: MenuItem? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_tlog_position_viewer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        toleranceInPixels = scaleDpToPixels(15.0).toDouble()

        val fm = childFragmentManager
        tlogEventMap = fm.findFragmentById(R.id.tlog_map_container) as TLogEventMapFragment?
        if(tlogEventMap == null){
            tlogEventMap = TLogEventMapFragment()
            fm.beginTransaction().add(R.id.tlog_map_container, tlogEventMap).commit()
        }

        eventsView?.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        }

        tlogPositionAdapter = TLogPositionEventAdapter(context, eventsView!!)
        eventsView?.adapter = tlogPositionAdapter

        fastScroller.setRecyclerView(eventsView!!)
        tlogPositionAdapter?.setTLogEventClickListener(this)

        val goToMyLocation = view.findViewById(R.id.my_location_button) as FloatingActionButton
        goToMyLocation.setOnClickListener {
            tlogEventMap?.goToMyLocation();
        }

        view.findViewById(R.id.drone_location_button)?.visibility = View.GONE

        // Setup the zoom to fit button
        view.findViewById(R.id.zoom_to_fit_button)?.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                tlogEventMap?.zoomToFit()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater){
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_tlog_position_viewer, menu)
        missionExportMenuItem = menu.findItem(R.id.menu_export_mission)
        missionExportMenuItem?.apply {
            isVisible = currentState == STATE_DATA_LOADED
            isEnabled = currentState == STATE_DATA_LOADED
        }
    }

    override fun onOptionsItemSelected(item : MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_export_mission -> {
                // Generate a mission from the drone historical gps position.
                val events = tlogPositionAdapter?.getItems() ?: return true
                val positions = mutableListOf<SpaceTime>()
                for(event in events){
                    if(event == null)
                        continue

                    val spaceTime = tlogEventToSpaceTime(event) ?: continue
                    positions.add(spaceTime)
                }

                val missionItems = MapUtils.exportPathAsMissionItems(positions, 0.00012)

                val missionProxy = missionProxy
                missionProxy.clear()
                missionProxy.addMissionItems(missionItems)

                startActivity(Intent(activity, EditorActivity::class.java))
                Toast.makeText(context, R.string.warning_check_exported_mission, Toast.LENGTH_LONG).show()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun scaleDpToPixels(value: Double): Int {
        val scale = resources.displayMetrics.density
        return Math.round(value * scale).toInt()
    }

    override fun onClearTLogData() {
        tlogPositionAdapter?.clear()
        lastEventTimestamp = -1L
        stateNoData()

        tlogEventMap?.onClearTLogData()
    }

    override fun onTLogSelected(tlogSession: SessionContract.SessionData) {
        tlogPositionAdapter?.clear()
        lastEventTimestamp = -1L
        stateLoadingData()

        // Refresh the map.
        tlogEventMap?.onTLogSelected(tlogSession)
    }

    override fun onApiDisconnected() {
        super.onApiDisconnected()
        lastEventTimestamp = -1L
    }

    override fun onTLogDataLoaded(events: List<TLogParser.Event>, hasMore: Boolean) {
        // Parse the event list and retrieve only the position events.
        newPositionEvents.clear()

        for(event in events){
            if(event.mavLinkMessage is msg_global_position_int) {
                // Events should be at least 1 second apart.
                if(lastEventTimestamp == -1L || (event.timestamp/1000 - lastEventTimestamp/1000) >= 1L){
                    lastEventTimestamp = event.timestamp
                    newPositionEvents.add(event)
                }
            }
        }

        // Refresh the adapter
        tlogPositionAdapter?.addItems(newPositionEvents)
        tlogPositionAdapter?.setHasMoreData(hasMore)

        if(tlogPositionAdapter?.itemCount == 0){
            if(hasMore){
                stateLoadingData()
            }
            else {
                stateNoData()
            }
        } else {
            stateDataLoaded()
        }

        tlogEventMap?.onTLogDataLoaded(newPositionEvents, hasMore)
    }

    override fun onTLogEventSelected(event: TLogParser.Event?) {
        //Propagate the click event to the map
        tlogEventMap?.onTLogEventSelected(event)
    }

    private fun stateLoadingData() {
        currentState = STATE_LOADING_DATA

        noDataView?.visibility = View.GONE
        eventsView?.visibility = View.GONE
        fastScroller.visibility = View.GONE
        loadingData?.visibility = View.VISIBLE

        missionExportMenuItem?.apply {
            setVisible(false)
            setEnabled(false)
        }
    }

    private fun stateNoData(){
        currentState = STATE_NO_DATA

        noDataView?.visibility = View.VISIBLE
        eventsView?.visibility = View.GONE
        fastScroller.visibility = View.GONE
        loadingData?.visibility = View.GONE

        missionExportMenuItem?.apply {
            setVisible(false)
            setEnabled(false)
        }
    }

    private fun stateDataLoaded(){
        currentState = STATE_DATA_LOADED

        noDataView?.visibility = View.GONE
        eventsView?.visibility = View.VISIBLE
        fastScroller.visibility = View.VISIBLE
        loadingData?.visibility = View.GONE

        missionExportMenuItem?.apply {
            setVisible(true)
            setEnabled(true)
        }
    }
}

