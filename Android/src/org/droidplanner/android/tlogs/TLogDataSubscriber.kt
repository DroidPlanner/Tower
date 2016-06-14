package org.droidplanner.android.tlogs

import com.o3dr.android.client.utils.data.tlog.TLogParser

/**
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
interface TLogDataSubscriber {
    fun onTLogDataLoaded(events: List<TLogParser.Event>)
}