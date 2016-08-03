package org.droidplanner.android.tlog

import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.o3dr.android.client.utils.data.tlog.TLogParser
import org.droidplanner.android.R
import org.droidplanner.android.activities.DrawerNavigationUI
import org.droidplanner.android.droneshare.data.SessionContract
import org.droidplanner.android.droneshare.data.SessionContract.SessionData
import org.droidplanner.android.tlog.adapters.TLogDataAdapter
import org.droidplanner.android.tlog.adapters.TLogViewerAdapter
import org.droidplanner.android.tlog.interfaces.TLogDataProvider
import org.droidplanner.android.tlog.viewers.TLogViewer
import timber.log.Timber
import java.util.*

/**
 * Created by fredia on 6/12/16.
 */
class TLogActivity : DrawerNavigationUI(), TLogDataAdapter.Listener, TLogDataProvider {

    companion object {
        private const val EXTRA_LOADED_EVENTS = "extra_loaded_events"
        private const val EXTRA_LOADING_DATA = "extra_loading_data"
        const val EXTRA_CURRENT_SESSION_ID = "extra_current_session_id"
    }

    private val handler = Handler()

    private val tlogSubscribers = HashSet<TLogViewer>()
    private val loadedEvents = ArrayList<TLogParser.Event>(100000)

    private var isLoadingData = false
    private var dataLoader: TLogDataLoader? = null
    private var currentSessionData: SessionData? = null

    private val loadingProgress by lazy {
        findViewById(R.id.progress_bar_container)
    }

    private val sessionTitleView by lazy {
        findViewById(R.id.tlog_session_title) as TextView?
    }

    override fun getNavigationDrawerMenuItemId() = R.id.navigation_locator

    override fun getToolbarId() = R.id.toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tlog)

        val viewPager = findViewById(R.id.container) as ViewPager?
        viewPager?.adapter = TLogViewerAdapter(supportFragmentManager)

        val tabLayout = findViewById(R.id.tabs) as TabLayout?
        tabLayout?.setupWithViewPager(viewPager)

        // Reload the loaded tlog events (if they exists)
        if (savedInstanceState != null) {

            val sessionId = savedInstanceState.getLong(EXTRA_CURRENT_SESSION_ID, -1L)
            if(sessionId != -1L){
                currentSessionData = dpApp.sessionDatabase.getSessionData(sessionId)
            }

            val wasLoadingData = savedInstanceState.getBoolean(EXTRA_LOADING_DATA)
            if(wasLoadingData){
                if(currentSessionData != null){
                    onTLogSelected(currentSessionData!!, true)
                }
            }
            else{
                val savedEvents = savedInstanceState.getSerializable(EXTRA_LOADED_EVENTS) as ArrayList<TLogParser.Event>?
                if (savedEvents != null) {
                    loadedEvents.addAll(savedEvents)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_locator, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_open_tlog_file -> {
                // Open a dialog showing the app generated tlog files
                val tlogPicker = TLogDataPicker()
                tlogPicker.arguments = Bundle().apply{
                    putLong(EXTRA_CURRENT_SESSION_ID, currentSessionData?.id?: -1L)
                }
                tlogPicker.show(supportFragmentManager, "TLog Data Picker")
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(EXTRA_LOADING_DATA, isLoadingData)

        if (!isLoadingData && loadedEvents.isNotEmpty()) {
            outState.putSerializable(EXTRA_LOADED_EVENTS, loadedEvents)
        }

        if(currentSessionData != null){
            outState.putLong(EXTRA_CURRENT_SESSION_ID, currentSessionData!!.id)
        }
    }

    override fun onTLogSelected(tlogSession: SessionData){
        onTLogSelected(tlogSession, false)
    }

    private fun onTLogSelected(tlogSession: SessionContract.SessionData, force: Boolean) {
        if(!force && tlogSession.equals(currentSessionData))
            return

        currentSessionData = tlogSession
        sessionTitleView?.text = TLogDataAdapter.dateFormatter.format(Date(tlogSession.startTime))
        sessionTitleView?.visibility = View.VISIBLE

        // Load the events from the selected tlog file
        Timber.i("Loading tlog data from ${tlogSession.tlogLoggingUri.path}")

        dataLoader?.cancel(true)

        // Show a loading progress bar
        loadingProgress?.visibility = View.VISIBLE
        loadedEvents.clear()

        dataLoader = TLogDataLoader(this, handler)
        dataLoader?.execute(tlogSession.tlogLoggingUri)

        isLoadingData = true
        notifyTLogSelected(tlogSession)
    }

    override fun onStop() {
        super.onStop()
        dataLoader?.cancel(true)
        dataLoader = null
    }

    override fun registerForTLogDataUpdate(subscriber: TLogViewer) {
        subscriber.onTLogDataLoaded(loadedEvents, isLoadingData)
        tlogSubscribers.add(subscriber)
    }

    override fun unregisterForTLogDataUpdate(subscriber: TLogViewer) {
        tlogSubscribers.remove(subscriber)
    }

    private fun notifyTLogSelected(tlogSession: SessionContract.SessionData) {
        for (subscriber in tlogSubscribers) {
            subscriber.onTLogSelected(tlogSession)
        }
    }

    private fun notifyTLogSubscribers(loadedEvents: List<TLogParser.Event>, hasMore: Boolean) {
        for (subscriber in tlogSubscribers) {
            subscriber.onTLogDataLoaded(loadedEvents, hasMore)
        }
    }

    fun onTLogLoadedData(newItems: List<TLogParser.Event>, hasMore: Boolean) {
        loadedEvents.addAll(newItems)

        if (hasMore) {
            Timber.i("Adding ${newItems.size} items")
        } else {
            Timber.i("Loaded ${loadedEvents.size} tlog events")
        }

        isLoadingData = hasMore
        notifyTLogSubscribers(newItems, hasMore)
        loadingProgress?.visibility = if(hasMore) View.VISIBLE else View.GONE
    }
}