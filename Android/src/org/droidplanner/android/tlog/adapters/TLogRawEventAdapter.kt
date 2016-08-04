package org.droidplanner.android.tlog.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.o3dr.android.client.utils.data.tlog.TLogParser
import org.droidplanner.android.R
import org.droidplanner.android.view.adapterViews.AbstractRecyclerViewFooterAdapter
import org.droidplanner.android.view.adapterViews.OnLoadMoreListener
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
class TLogRawEventAdapter(recyclerView: RecyclerView, onLoadMoreListener: OnLoadMoreListener?) :
        AbstractRecyclerViewFooterAdapter<TLogParser.Event>(recyclerView, onLoadMoreListener) {

    class ViewHolder(eventView: View, val eventInfo: TextView, val eventTimestamp: TextView) :
            RecyclerView.ViewHolder(eventView)

    companion object {
        private val dateFormatter = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US)
    }

    fun clear(hasMore: Boolean = true){
        resetItems(null)
        setHasMoreData(hasMore)
    }

    override fun onBindBasicItemView(genericHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = genericHolder as ViewHolder
        val event = getItem(position)
        holder.eventInfo.text = event.mavLinkMessage.toString()
        holder.eventTimestamp.text = dateFormatter.format(Date(event.timestamp))
    }

    override fun onCreateBasicItemViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val eventView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_tlog_raw_event, parent, false)
        val eventTimestamp = eventView.findViewById(R.id.event_timestamp) as TextView
        val eventInfo = eventView.findViewById(R.id.event_info) as TextView
        return ViewHolder(eventView, eventInfo, eventTimestamp)
    }

    override fun onCreateFooterViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        //noinspection ConstantConditions
        val v = LayoutInflater.from(parent.context).inflate(R.layout.progress_bar, parent, false)
        val progressBar = v.findViewById(R.id.progressBar) as ProgressBar
        return ProgressViewHolder(v, progressBar)
    }

    override fun onBindFooterView(genericHolder: RecyclerView.ViewHolder, position: Int) {
        (genericHolder as ProgressViewHolder).progressBar.isIndeterminate = true
    }

    class ProgressViewHolder(v: View, val progressBar: ProgressBar) : RecyclerView.ViewHolder(v)
}