package com.o3dr.android.client.utils.data.tlog;

import java.util.List;

/**
 * Callback for asynchronous TLog parser.
 */
public interface TLogParserCallback {

    /**
     * Callback for successful retrieval of one or more Event.
     *
     * @param events {@link com.o3dr.android.client.utils.data.tlog.TLogParser.Event}
     */
    void onResult(List<TLogParser.Event> events);

    /**
     * Callback for unsuccessful retrieval of Events.
     * {@link java.util.NoSuchElementException} is returned when the tlogs contain no Events
     * matching the criteria.
     *
     * @param e
     */
    void onFailed(Exception e);
}