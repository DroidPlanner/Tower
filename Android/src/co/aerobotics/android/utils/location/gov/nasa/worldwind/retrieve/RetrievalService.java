/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package co.aerobotics.android.utils.location.gov.nasa.worldwind.retrieve;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.WWObject;

/**
 * @author Tom Gaskins
 * @version $Id$
 */
public interface RetrievalService extends WWObject
{
    /**
     * Schedule a retrieval task.
     *
     * @param retriever the retriever to run
     *
     * @return a future object that can be used to query the request status of cancel the request.
     *
     * @throws IllegalArgumentException if <code>retriever</code> is null or has no name
     */
    RetrievalFuture runRetriever(Retriever retriever);

    /**
     * Schedule a retrieval task, with a priority.
     *
     * @param retriever the retriever to run
     * @param priority  the secondary priority of the retriever, or negative if it is to be the primary priority
     *
     * @return a future object that can be used to query the request status of cancel the request.
     *
     * @throws IllegalArgumentException if <code>retriever</code> is null or has no name
     */
    RetrievalFuture runRetriever(Retriever retriever, double priority);

    /**
     * Specifies the size of the retrieval thread pool.
     *
     * @param poolSize the number of threads in the thread pool
     *
     * @throws IllegalArgumentException if <code>poolSize</code> is non-positive
     */
    void setRetrieverPoolSize(int poolSize);

    /**
     * Indicates the size of the retrieval thread pool.
     *
     * @return Size of the retrieval thread pool.
     */
    int getRetrieverPoolSize();

    /**
     * Indicates whether or not the service has actively running tasks.
     *
     * @return {@code true} if there are retrievers running, {@code false} if not.
     */
    boolean hasActiveTasks();

    /**
     * Indicates whether or not the retrieval service is available.
     *
     * @return {@code true} if the retrieval service is available, or {@code false} if the service is not available.
     */
    boolean isAvailable();

    /**
     * @param retriever the retriever to check
     *
     * @return <code>true</code> if the retriever is being run or pending execution
     *
     * @throws IllegalArgumentException if <code>retriever</code> is null
     */
    boolean contains(Retriever retriever);

    /**
     * Indicates the number of retrieval tasks that have not completed or been cancelled.
     *
     * @return The number of retrieval tasks that are running or waiting to run.
     */
    int getNumRetrieversPending();

    /**
     * Shutdown the retrieval service.
     *
     * @param immediately Indicates whether or not to interrupt running tasks. A value of {@code true} indicates that
     *                    the tasks should be interrupted.
     */
    void shutdown(boolean immediately);
}
