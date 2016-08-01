package org.droidplanner.android.tlog.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.droidplanner.android.DroidPlannerApp
import org.droidplanner.android.R
import org.droidplanner.android.droneshare.data.SessionContract.SessionData
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by fhuya on 6/12/2016.
 */
class TLogDataAdapter(app: DroidPlannerApp) : RecyclerView.Adapter<TLogDataAdapter.ViewHolder>() {

    companion object {
        private val dateFormatter = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US)
    }

    interface TLogSelectionListener {
        fun onTLogSelected(tlogSession: SessionData)
    }

    class ViewHolder(val container: View, val dataTimestamp: TextView) : RecyclerView.ViewHolder(container)

    private var tlogSelectionListener : TLogSelectionListener? = null
    private val completedSessions = app.sessionDatabase.getCompletedSessions(true)

    fun setTLogSelectionListener(listener: TLogSelectionListener?){
        this.tlogSelectionListener = listener
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sessionData = completedSessions[position]
        holder.dataTimestamp.text = dateFormatter.format(Date(sessionData.startTime))
        holder.dataTimestamp.setOnClickListener {
            // Notify the listener of the selected tlog file
            tlogSelectionListener?.onTLogSelected(sessionData)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder? {
        val containerView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_tlog_data, parent, false)
        val dataTimestamp = containerView.findViewById(R.id.tlog_data_timestamp) as TextView
        return ViewHolder(containerView, dataTimestamp)
    }

    override fun getItemCount() = completedSessions.size
}