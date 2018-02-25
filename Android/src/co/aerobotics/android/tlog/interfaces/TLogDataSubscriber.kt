package co.aerobotics.android.tlog.interfaces

import com.o3dr.android.client.utils.data.tlog.TLogParser

/**
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
interface TLogDataSubscriber {
    fun onTLogSelected(tlogSession: co.aerobotics.android.droneshare.data.SessionContract.SessionData)
    fun onTLogDataLoaded(events: List<TLogParser.Event>, hasMore: Boolean = true)
    fun onClearTLogData()
}