package org.droidplanner.android.tlog.adapters

import android.support.v4.app.FragmentManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.droidplanner.android.DroidPlannerApp
import org.droidplanner.android.R
import org.droidplanner.android.dialogs.OkDialog
import org.droidplanner.android.droneshare.data.SessionContract.SessionData
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by fhuya on 6/12/2016.
 */
class TLogDataAdapter(val app: DroidPlannerApp, val fragmentMgr: FragmentManager, val selectedSessionId: Long) :
        RecyclerView.Adapter<TLogDataAdapter.ViewHolder>() {

    companion object {
        private val dateFormatter = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US)
    }

    interface Listener {
        fun onTLogSelected(tlogSession: SessionData)
    }

    class ViewHolder(val container: View, val dataTimestamp: TextView, val clearSession: View) : RecyclerView.ViewHolder(container)

    private var tlogSelectionListener: Listener? = null
    private var completedSessions = app.sessionDatabase.getCompletedSessions(true)

    fun setTLogSelectionListener(listener: Listener?) {
        this.tlogSelectionListener = listener
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sessionData = completedSessions[position]

        holder.container.isActivated = sessionData.id == selectedSessionId

        val sessionDate = dateFormatter.format(Date(sessionData.startTime))
        holder.dataTimestamp.text = sessionDate
        holder.dataTimestamp.setOnClickListener {
            // Notify the listener of the selected tlog file
            tlogSelectionListener?.onTLogSelected(sessionData)
        }

        holder.clearSession.setOnClickListener {
            // Confirm before deletion
            val confirmDialog = OkDialog.newInstance(app.applicationContext, "Delete?", "Delete session ${sessionDate}?", object : OkDialog.Listener{
                override fun onOk() {
                    // Remove the session data entry from the database.
                    app.sessionDatabase.removeSessionData(sessionData.id)
                    reloadCompletedSessions()
                }

                override fun onCancel() {}

                override fun onDismiss() {}

            }, true)
            confirmDialog.show(fragmentMgr, "Delete tlog session")
        }
    }

    private fun reloadCompletedSessions() {
        completedSessions = app.sessionDatabase.getCompletedSessions(true)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder? {
        val containerView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_tlog_data, parent, false)
        val dataTimestamp = containerView.findViewById(R.id.tlog_data_timestamp) as TextView
        val clearSession = containerView.findViewById(R.id.clear_tlog_session)
        return ViewHolder(containerView, dataTimestamp, clearSession)
    }

    override fun getItemCount() = completedSessions.size
}