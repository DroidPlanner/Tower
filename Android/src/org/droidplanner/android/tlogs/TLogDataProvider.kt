package org.droidplanner.android.tlogs

/**
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
interface TLogDataProvider {
    fun registerForTLogDataUpdate(subscriber: TLogDataSubscriber)
    fun unregisterForTLogDataUpdate(subscriber: TLogDataSubscriber)
}