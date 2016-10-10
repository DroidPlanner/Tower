package org.droidplanner.android.tlog

import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.o3dr.android.client.utils.data.tlog.TLogParser
import org.droidplanner.android.R
import org.droidplanner.android.activities.DrawerNavigationUI
import org.droidplanner.android.dialogs.OkDialog
import org.droidplanner.android.dialogs.SupportEditInputDialog
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
        const val EXTRA_CURRENT_SESSION_ID = "extra_current_session_id"

        const val INVALID_SESSION_ID = -1L
    }

    private val handler = Handler()

    private val tlogSubscribers = HashSet<TLogViewer>()
    private val loadedEvents = LinkedList<TLogParser.Event>()

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

    override fun getToolbarId() = R.id.actionbar_toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tlog)

        val viewPager = findViewById(R.id.container) as ViewPager?
        viewPager?.adapter = TLogViewerAdapter(supportFragmentManager)

        val tabLayout = findViewById(R.id.tabs) as TabLayout?
        tabLayout?.setupWithViewPager(viewPager)

        // Reload the loaded tlog events
        val sessionId = savedInstanceState
                ?.getLong(EXTRA_CURRENT_SESSION_ID, mAppPrefs.vehicleHistorySessionId)
                ?: mAppPrefs.vehicleHistorySessionId
        if (sessionId != INVALID_SESSION_ID) {
            currentSessionData = dpApp.sessionDatabase.getSessionData(sessionId)
            if (currentSessionData != null) {
                onTLogSelected(currentSessionData!!, true)
            }
        }
    }

    override fun addToolbarFragment(){}

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
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

            R.id.menu_rename_tlog_session -> {
                if (currentSessionData != null) {
                    val renameDialog = SupportEditInputDialog.newInstance(TLogDataAdapter.RENAME_SESSION_TAG,
                            "Enter session label", currentSessionData!!.label, true,
                            object : SupportEditInputDialog.Listener{
                                override fun onOk(dialogTag: String?, input: CharSequence?) {
                                    if (TextUtils.isEmpty(input)) {
                                        Toast.makeText(applicationContext, R.string.warning_invalid_session_label_entry, Toast.LENGTH_LONG).show();
                                    } else if (currentSessionData!!.label != input) {
                                        dpApp.sessionDatabase.renameSession(currentSessionData!!.id, input.toString())
                                        onTLogRenamed(currentSessionData!!.id, input.toString())
                                    }
                                }

                                override fun onCancel(dialogTag: String?) {}

                            })
                    renameDialog.show(supportFragmentManager, TLogDataAdapter.RENAME_SESSION_TAG)
                }
                return true
            }

            R.id.menu_delete_tlog_session -> {
                if (currentSessionData != null) {
                    val confirmDialog = OkDialog.newInstance(applicationContext, "Delete?",
                            "Delete session ${currentSessionData!!.label}?",
                            object : OkDialog.Listener {
                                override fun onOk() {
                                    // Remove the session data entry from the database.
                                    dpApp.sessionDatabase.removeSessionData(currentSessionData!!.id)
                                    onTLogDeleted(currentSessionData!!.id)
                                }

                                override fun onCancel() {
                                }

                                override fun onDismiss() {
                                }

                            }, true)
                    confirmDialog.show(supportFragmentManager, "Delete tlog session")
                }
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if(currentSessionData != null){
            outState.putLong(EXTRA_CURRENT_SESSION_ID, currentSessionData!!.id)
        }
    }

    override fun onTLogSelected(tlogSession: SessionData){
        onTLogSelected(tlogSession, false)
    }

    override fun onTLogRenamed(sessionId:Long, sessionLabel : String) {
        if (sessionId == currentSessionData?.id) {
            sessionTitleView?.text = sessionLabel
        }
    }

    override fun onTLogDeleted(sessionId : Long) {
        if (sessionId == currentSessionData?.id) {
            mAppPrefs.saveVehicleHistorySessionId(INVALID_SESSION_ID)
            currentSessionData = null
            sessionTitleView?.visibility = View.GONE

            dataLoader?.cancel(true)

            loadingProgress?.visibility = View.GONE
            loadedEvents.clear()
            isLoadingData = false

            notifyTLogDataDeleted()
        }
    }

    private fun onTLogSelected(tlogSession: SessionContract.SessionData, force: Boolean) {
        if(!force && tlogSession.equals(currentSessionData))
            return

        mAppPrefs.saveVehicleHistorySessionId(tlogSession.id)
        currentSessionData = tlogSession
        sessionTitleView?.text = tlogSession.label
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

    private fun notifyTLogDataDeleted(){
        for (subscriber in tlogSubscribers) {
            subscriber.onClearTLogData()
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