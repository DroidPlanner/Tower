package org.droidplanner.android.tlogs

import android.support.v4.app.Fragment
import com.o3dr.android.client.utils.data.tlog.TLogParser

/**
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
class TLogPositionViewer : Fragment(), TLogDataSubscriber {
    override fun onTLogDataLoaded(events: List<TLogParser.Event>) {

    }
}