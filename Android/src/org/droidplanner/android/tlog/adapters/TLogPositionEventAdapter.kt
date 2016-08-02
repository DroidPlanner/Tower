package org.droidplanner.android.tlog.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.o3dr.android.client.utils.data.tlog.TLogParser
import org.droidplanner.android.R
import org.droidplanner.android.tlog.event.TLogEventListener
import org.droidplanner.android.view.adapterViews.AbstractRecyclerViewFooterAdapter
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
class TLogPositionEventAdapter(recyclerView: RecyclerView) :
        AbstractRecyclerViewFooterAdapter<TLogParser.Event>(recyclerView, null) {

    class ViewHolder(val container: View, val thumbnail: TextView) : RecyclerView.ViewHolder(container)

    companion object {
        private val dateFormatter = SimpleDateFormat("HH:mm:ss", Locale.US)
    }

    private var selectedEvent: Pair<Int, TLogParser.Event>? = null
    private var tlogEventListener: TLogEventListener? = null

    fun setTLogEventClickListener(listener: TLogEventListener?){
        tlogEventListener = listener
    }

    fun clear(hasMore: Boolean = true){
        resetItems(null)
        setHasMoreData(hasMore)
    }

    override fun onBindBasicItemView(genericHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = genericHolder as ViewHolder

        val event = getItem(position)
        holder.container.isActivated = event == selectedEvent?.second
        holder.thumbnail.text = dateFormatter.format(event.timestamp)
        holder.thumbnail.setOnClickListener {
            if(event == selectedEvent?.second){
                // Unselect the event
                selectedEvent = null
                tlogEventListener?.onTLogEventSelected(null)
                notifyItemChanged(position)
            }
            else {
                val previousPosition = selectedEvent?.first ?: -1
                selectedEvent = Pair(position, event)
                tlogEventListener?.onTLogEventSelected(event)
                notifyItemChanged(position)
                if(previousPosition != -1)
                    notifyItemChanged(previousPosition)
            }
        }
    }

    override fun onCreateBasicItemViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val container = LayoutInflater.from(parent.context).inflate(R.layout.list_item_tlog_position_event, parent, false)
        val thumbnail = container.findViewById(R.id.position_event_thumbnail) as TextView
        return ViewHolder(container, thumbnail)
    }

    override fun onCreateFooterViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val container = LayoutInflater.from(parent.context).inflate(R.layout.list_item_tlog_position_event_loading, parent, false)
        val progressBar = container.findViewById(R.id.progressBar) as ProgressBar
        return ProgressViewHolder(container, progressBar)
    }

    override fun onBindFooterView(genericHolder: RecyclerView.ViewHolder, position: Int) {
        (genericHolder as ProgressViewHolder).progressBar.isIndeterminate = true
    }

    class ProgressViewHolder(v: View, val progressBar: ProgressBar) : RecyclerView.ViewHolder(v)

}