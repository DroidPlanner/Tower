package org.droidplanner.android.tlog.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.o3dr.android.client.utils.data.tlog.TLogParser
import org.droidplanner.android.R
import org.droidplanner.android.tlog.event.TLogEventClickListener
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
class TLogPositionEventAdapter : RecyclerView.Adapter<TLogPositionEventAdapter.ViewHolder>() {

    class ViewHolder(val container: View, val thumbnail: TextView) : RecyclerView.ViewHolder(container)

    companion object {
        private val dateFormatter = SimpleDateFormat("HH:mm:ss", Locale.US)
    }

    private val positionEvents = ArrayList< TLogParser.Event>()

    private var tlogEventClickListener: TLogEventClickListener? = null

    fun setTLogEventClickListener(listener: TLogEventClickListener?){
        tlogEventClickListener = listener
    }

    fun loadTLogPositionEvents(events: List<TLogParser.Event>){
        positionEvents.clear()
        positionEvents.addAll(events)
        notifyDataSetChanged()
    }

    override fun getItemCount() = positionEvents.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = positionEvents[position]
        holder.thumbnail.text = dateFormatter.format(event.timestamp)
        holder.thumbnail.setOnClickListener {
            tlogEventClickListener?.onTLogEventClick(event)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder? {
        val container = LayoutInflater.from(parent.context).inflate(R.layout.list_item_tlog_position_event, parent, false)
        val thumbnail = container.findViewById(R.id.position_event_thumbnail) as TextView
        return ViewHolder(container, thumbnail)
    }

}