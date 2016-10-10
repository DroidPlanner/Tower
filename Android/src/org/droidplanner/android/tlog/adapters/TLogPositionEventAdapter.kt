package org.droidplanner.android.tlog.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.MAVLink.common.msg_global_position_int
import com.o3dr.android.client.utils.data.tlog.TLogParser
import org.droidplanner.android.R
import org.droidplanner.android.tlog.event.TLogEventListener
import org.droidplanner.android.tlog.viewers.TLogPositionViewer
import org.droidplanner.android.utils.unit.UnitManager
import org.droidplanner.android.utils.unit.providers.length.LengthUnitProvider
import org.droidplanner.android.view.adapterViews.AbstractRecyclerViewFooterAdapter
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
class TLogPositionEventAdapter(context : Context, recyclerView: RecyclerView) :
        AbstractRecyclerViewFooterAdapter<TLogParser.Event>(recyclerView, null) {

    class ViewHolder(val container: View, val thumbnail : View, val timestamp: TextView, val altitude: TextView) :
            RecyclerView.ViewHolder(container)

    companion object {
        private val dateFormatter = SimpleDateFormat("HH:mm:ss", Locale.US)
    }

    private val lessAltitudeIcon : Drawable
    private val sameAltitudeIcon : Drawable
    private val moreAltitudeIcon : Drawable
    private val lengthUnitProvider : LengthUnitProvider

    init {
        val res = context.resources
        lessAltitudeIcon = res.getDrawable(R.drawable.ic_file_download_black_24dp)
        sameAltitudeIcon = res.getDrawable(R.drawable.ic_remove_black_24dp)
        moreAltitudeIcon = res.getDrawable(R.drawable.ic_file_upload_grey_700_18dp)

        lengthUnitProvider = UnitManager.getUnitSystem(context).lengthUnitProvider
    }

    private var selectedEvent: Pair<Int, TLogParser.Event>? = null
    private var tlogEventListener: TLogEventListener? = null

    fun setTLogEventClickListener(listener: TLogEventListener?){
        tlogEventListener = listener
    }

    fun clear(hasMore: Boolean = true){
        selectedEvent = null
        resetItems(null)
        setHasMoreData(hasMore)
    }

    override fun onBindBasicItemView(genericHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = genericHolder as ViewHolder

        val event = getItem(position)
        holder.container.isActivated = event == selectedEvent?.second
        holder.timestamp.text = dateFormatter.format(event.timestamp)

        val previousAltitude = if (position == 0) null else TLogPositionViewer.getEventAltitude(getItem(position -1).mavLinkMessage as msg_global_position_int)
        val currentAltitude = TLogPositionViewer.getEventAltitude(event.mavLinkMessage as msg_global_position_int)

        val altIcon = if (previousAltitude == null || previousAltitude < currentAltitude) {
            moreAltitudeIcon
        } else if (previousAltitude == currentAltitude) {
            sameAltitudeIcon
        } else {
            lessAltitudeIcon
        }

        val convertedAltitude = lengthUnitProvider.boxBaseValueToTarget(currentAltitude)

        val altitudeText = String.format(Locale.US, "%2.2f%s", convertedAltitude.getValue(), convertedAltitude.getUnitSymbol())
        holder.altitude.text = altitudeText
        holder.altitude.setCompoundDrawablesWithIntrinsicBounds(altIcon, null, null, null)
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
        val thumbnail = container.findViewById(R.id.event_thumbnail)
        val timestamp = container.findViewById(R.id.event_timestamp) as TextView
        val altitude = container.findViewById(R.id.event_altitude) as TextView
        return ViewHolder(container, thumbnail, timestamp, altitude)
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