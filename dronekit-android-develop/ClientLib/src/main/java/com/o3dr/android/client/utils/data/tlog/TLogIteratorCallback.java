package com.o3dr.android.client.utils.data.tlog;

/**
 * Callback for asynchronous TLog iterator.
 */
public interface TLogIteratorCallback {

    /**
     * Callback for successful retrieval of next Event.
     *
     * @param event
     */
    void onResult(TLogParser.Event event);

    /**
     * Callback for unsuccessful retrieval of next Event.
     * {@link java.util.NoSuchElementException} is returned when the tlogs contain no more Events
     * matching the criteria.
     *
     * @param e
     */
    void onFailed(Exception e);
}