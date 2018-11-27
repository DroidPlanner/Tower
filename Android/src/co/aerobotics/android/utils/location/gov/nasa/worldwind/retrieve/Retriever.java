/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package co.aerobotics.android.utils.location.gov.nasa.worldwind.retrieve;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.WWObject;

import java.nio.ByteBuffer;

/**
 * @author Tom Gaskins
 * @version $Id$
 */
public interface Retriever extends WWObject, java.util.concurrent.Callable<Retriever>
{
    public final String RETRIEVER_STATE_NOT_STARTED = "gov.nasa.worldwind.RetrieverStatusNotStarted";
    public final String RETRIEVER_STATE_STARTED = "gov.nasa.worldwind.RetrieverStatusStarted";
    public final String RETRIEVER_STATE_CONNECTING = "gov.nasa.worldwind.RetrieverStatusConnecting";
    public final String RETRIEVER_STATE_READING = "gov.nasa.worldwind.RetrieverStatusReading";
    public final String RETRIEVER_STATE_INTERRUPTED = "gov.nasa.worldwind.RetrieverStatusInterrupted";
    public final String RETRIEVER_STATE_ERROR = "gov.nasa.worldwind.RetrieverStatusError";
    public final String RETRIEVER_STATE_SUCCESSFUL = "gov.nasa.worldwind.RetrieverStatusSuccessful";

    ByteBuffer getBuffer();

    int getContentLength();

    int getContentLengthRead();

    String getName();

    String getState();

    String getContentType();

    long getSubmitTime();

    void setSubmitTime(long submitTime);

    long getBeginTime();

    void setBeginTime(long beginTime);

    long getEndTime();

    void setEndTime(long endTime);

    int getConnectTimeout();

    int getReadTimeout();

    void setReadTimeout(int readTimeout);

    void setConnectTimeout(int connectTimeout);

    int getStaleRequestLimit();

    void setStaleRequestLimit(int staleRequestLimit);
}
