package org.droidplanner.android.tlog

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.o3dr.android.client.utils.data.tlog.TLogParser
import com.o3dr.android.client.utils.data.tlog.TLogParserCallback
import org.droidplanner.android.R
import org.droidplanner.android.activities.DrawerNavigationUI
import org.droidplanner.android.tlog.adapters.TLogDataAdapter
import org.droidplanner.android.tlog.adapters.TLogViewerAdapter
import org.droidplanner.android.tlog.interfaces.TLogDataProvider
import org.droidplanner.android.tlog.viewers.TLogViewer
import timber.log.Timber
import java.io.File
import java.util.*

/**
 * Created by fredia on 6/12/16.
 */
class TLogActivity : DrawerNavigationUI(), TLogDataAdapter.TLogSelectionListener, TLogDataProvider {

    private companion object {
        const val EXTRA_LOADED_EVENTS = "extra_loaded_events"
    }

    private val handler = Handler()

    private val tlogParserCb = object : TLogParserCallback {
        override fun onFailed(e: Exception?) {
            Timber.w("Unable to load tlog data: ${e?.message ?: ""}")
            loadedEvents.clear()
            notifyTLogSubscribers()
            loadingProgress?.visibility = View.VISIBLE
        }

        override fun onResult(events: MutableList<TLogParser.Event>) {
            Timber.i("Loaded ${events.size} tlog events")
            loadedEvents.clear()
            loadedEvents.addAll(events)
            notifyTLogSubscribers()
            loadingProgress?.visibility = View.VISIBLE
        }
    }

    private val tlogSubscribers = HashSet<TLogViewer>()
    private val loadedEvents = ArrayList<TLogParser.Event>()

    private val loadingProgress by lazy {
        findViewById(R.id.progress_bar_container)
    }

    override fun getNavigationDrawerMenuItemId() = R.id.navigation_locator

    override fun getToolbarId() = R.id.toolbar

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tlog)

        val viewPager = findViewById(R.id.container) as ViewPager?
        viewPager?.adapter = TLogViewerAdapter(supportFragmentManager)

        val tabLayout = findViewById(R.id.tabs) as TabLayout?
        tabLayout?.setupWithViewPager(viewPager)

        // Reload the loaded tlog events (if they exists)
        if(savedInstanceState != null){
            val savedEvents = savedInstanceState.getSerializable(EXTRA_LOADED_EVENTS) as ArrayList<TLogParser.Event>?
            if (savedEvents != null){
                loadedEvents.addAll(savedEvents)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_locator, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
         when(item.itemId){
            R.id.menu_open_tlog_file -> {
                // Open a dialog showing the app generated tlog files
                val tlogPicker = TLogDataPicker()
                tlogPicker.show(supportFragmentManager, "TLog Data Picker")
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle){
        super.onSaveInstanceState(outState)
        if(loadedEvents.isNotEmpty()){
            outState.putSerializable(EXTRA_LOADED_EVENTS, loadedEvents)
        }
    }

    override fun onTLogSelected(tlogFile: File) {
        // Load the events from the selected tlog file
        Timber.i("Loading tlog data from ${tlogFile.name}")

        // TODO: Show a loading progress bar
        loadingProgress?.visibility = View.VISIBLE
        TLogParser.getAllEventsAsync(handler, Uri.fromFile(tlogFile), tlogParserCb)
    }

    override fun registerForTLogDataUpdate(subscriber: TLogViewer) {
        subscriber.onTLogDataLoaded(loadedEvents)
        tlogSubscribers.add(subscriber)
    }

    override fun unregisterForTLogDataUpdate(subscriber: TLogViewer) {
        tlogSubscribers.remove(subscriber)
    }

    private fun notifyTLogSubscribers(){
        for(subscriber in tlogSubscribers){
            subscriber.onTLogDataLoaded(loadedEvents)
        }
    }
}