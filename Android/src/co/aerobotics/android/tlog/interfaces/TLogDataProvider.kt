package co.aerobotics.android.tlog.interfaces

import co.aerobotics.android.tlog.viewers.TLogViewer

/**
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
interface TLogDataProvider {
    fun registerForTLogDataUpdate(subscriber: TLogViewer)
    fun unregisterForTLogDataUpdate(subscriber: TLogViewer)
}