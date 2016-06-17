package org.droidplanner.android.tlogs.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.o3dr.android.client.utils.data.tlog.TLogParser
import org.droidplanner.android.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
class TLogRawEventAdapter : RecyclerView.Adapter<TLogRawEventAdapter.ViewHolder>() {

    class ViewHolder(eventView: View, val eventInfo: TextView, val eventTimestamp: TextView) :
            RecyclerView.ViewHolder(eventView)

    companion object {
        private val dateFormatter = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US)
    }
    private val loadedEvents = ArrayList<TLogParser.Event>()

    fun loadTLogEvents(events: List<TLogParser.Event>){
        loadedEvents.clear()
        loadedEvents.addAll(events)
        notifyDataSetChanged()
    }

    override fun getItemCount() = loadedEvents.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = loadedEvents[position]
        holder.eventInfo.text = event.mavLinkMessage.toString()
        holder.eventTimestamp.text = dateFormatter.format(Date(event.timestamp))
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder? {
        val eventView = LayoutInflater.from(parent?.context).inflate(R.layout.list_item_tlog_raw_event, parent, false)
        val eventTimestamp = eventView.findViewById(R.id.event_timestamp) as TextView
        val eventInfo = eventView.findViewById(R.id.event_info) as TextView
        return ViewHolder(eventView, eventInfo, eventTimestamp)
    }
}