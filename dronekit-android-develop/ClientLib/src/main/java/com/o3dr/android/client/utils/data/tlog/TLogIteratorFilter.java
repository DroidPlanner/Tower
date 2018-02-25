package com.o3dr.android.client.utils.data.tlog;

/**
 * Filter class for TLog iterator to allow the caller to determine the criteria for returned event.
 */
public interface TLogIteratorFilter {
    /**
     * This method is called when an event is parsed to determine whether the caller wants this result
     * returned.
     *
     * @param event {@link com.o3dr.android.client.utils.data.tlog.TLogParser.Event}
     * @return whether this event should be accepted based off criteria
     */
    boolean acceptEvent(TLogParser.Event event);
}