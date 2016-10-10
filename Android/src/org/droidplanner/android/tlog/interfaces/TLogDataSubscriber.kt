package org.droidplanner.android.tlog.interfaces

import com.o3dr.android.client.utils.data.tlog.TLogParser
import org.droidplanner.android.droneshare.data.SessionContract

/**
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
interface TLogDataSubscriber {
    fun onTLogSelected(tlogSession: SessionContract.SessionData)
    fun onTLogDataLoaded(events: List<TLogParser.Event>, hasMore: Boolean = true)
    fun onClearTLogData()
}