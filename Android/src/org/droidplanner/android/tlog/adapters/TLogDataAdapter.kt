package org.droidplanner.android.tlog.adapters

import android.support.v4.app.FragmentManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import org.droidplanner.android.DroidPlannerApp
import org.droidplanner.android.R
import org.droidplanner.android.dialogs.OkDialog
import org.droidplanner.android.dialogs.SupportEditInputDialog
import org.droidplanner.android.droneshare.data.SessionContract.SessionData

/**
 * Created by fhuya on 6/12/2016.
 */
class TLogDataAdapter(val app: DroidPlannerApp, val fragmentMgr: FragmentManager, val selectedSessionId: Long) :
        RecyclerView.Adapter<TLogDataAdapter.ViewHolder>() {

    interface Listener {
        fun onTLogSelected(tlogSession: SessionData)
        fun onTLogRenamed(sessionId: Long, sessionLabel : String)
        fun onTLogDeleted(sessionId: Long)
    }

    companion object {
        const val RENAME_SESSION_TAG = "Rename tlog session"
    }

    class ViewHolder(val container: View, val dataLabel: TextView, val clearSession: View, val editLabel: View)
    : RecyclerView.ViewHolder(container)

    private var tlogSelectionListener: Listener? = null
    private var completedSessions = app.sessionDatabase.getCompletedSessions(true)

    fun setTLogSelectionListener(listener: Listener?) {
        this.tlogSelectionListener = listener
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sessionData = completedSessions[position]

        holder.container.isActivated = sessionData.id == selectedSessionId

        holder.dataLabel.text = sessionData.label
        holder.dataLabel.setOnClickListener {
            // Notify the listener of the selected tlog file
            tlogSelectionListener?.onTLogSelected(sessionData)
        }

        holder.clearSession.setOnClickListener {
            // Confirm before deletion
            val confirmDialog = OkDialog.newInstance(app.applicationContext, "Delete?", "Delete session ${sessionData.label}?", object : OkDialog.Listener{
                override fun onOk() {
                    // Remove the session data entry from the database.
                    app.sessionDatabase.removeSessionData(sessionData.id)
                    tlogSelectionListener?.onTLogDeleted(sessionData.id)
                    reloadCompletedSessions()
                }

                override fun onCancel() {}

                override fun onDismiss() {}

            }, true)
            confirmDialog.show(fragmentMgr, "Delete tlog session")
        }

        holder.editLabel.setOnClickListener {
            // Bring up edit text dialog with the current label
            val renameDialog = SupportEditInputDialog.newInstance(RENAME_SESSION_TAG,
                    "Enter session label", sessionData.label, true,
                    object : SupportEditInputDialog.Listener{
                override fun onOk(dialogTag: String?, input: CharSequence?) {
                    if (TextUtils.isEmpty(input)) {
                        Toast.makeText(app.applicationContext, R.string.warning_invalid_session_label_entry, Toast.LENGTH_LONG).show();
                    } else if (sessionData.label != input) {
                        app.sessionDatabase.renameSession(sessionData.id, input.toString())
                        tlogSelectionListener?.onTLogRenamed(sessionData.id, input.toString())
                        reloadCompletedSessions()
                    }
                }

                override fun onCancel(dialogTag: String?) {}

            })
            renameDialog.show(fragmentMgr, RENAME_SESSION_TAG)
        }
    }

    private fun reloadCompletedSessions() {
        completedSessions = app.sessionDatabase.getCompletedSessions(true)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder? {
        val containerView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_tlog_data, parent, false)
        val dataLabel = containerView.findViewById(R.id.tlog_data_label) as TextView
        val clearSession = containerView.findViewById(R.id.clear_tlog_session)
        val editLabel = containerView.findViewById(R.id.rename_tlog_session)
        return ViewHolder(containerView, dataLabel, clearSession, editLabel)
    }

    override fun getItemCount() = completedSessions.size

    fun getIndexFor(sessionId: Long): Int {
        for (i in 0 until completedSessions.size) {
            val session = completedSessions[i]
            if (session.id == sessionId)
                return i
        }
        return -1
    }
}