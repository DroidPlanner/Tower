package org.droidplanner.android.tlog.event

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.o3dr.android.client.utils.data.tlog.TLogParser
import org.droidplanner.android.R

/**
 * TODO: Complete implementation for the TLog event detail class
 */
class TLogEventDetail : Fragment(), TLogEventListener {

    private val eventInfoContainer by lazy {
        getView()?.findViewById(R.id.event_detail_container)
    }

    private val eventInfoView by lazy {
        getView()?.findViewById(R.id.event_info_dump) as TextView?
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tlog_event_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onTLogEventSelected(event: TLogParser.Event?) {
        eventInfoContainer?.visibility = if(event == null) View.GONE else View.VISIBLE
        if(event != null) {
            eventInfoView?.setText(event.mavLinkMessage.toString())
        }
    }

}