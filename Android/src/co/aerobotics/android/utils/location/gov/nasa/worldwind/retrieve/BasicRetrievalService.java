/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package co.aerobotics.android.utils.location.gov.nasa.worldwind.retrieve;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.avlist.AVKey;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.Logging;

import java.net.*;
import java.util.concurrent.*;

/**
 * Performs threaded retrieval of data.
 *
 * @author Tom Gaskins
 * @version $Id$
 */
public class BasicRetrievalService extends WWObjectImpl
    implements RetrievalService, Thread.UncaughtExceptionHandler
{
    // These constants are last-ditch values in case Configuration lacks defaults
    protected static final int DEFAULT_QUEUE_SIZE = 100;
    protected static final int DEFAULT_POOL_SIZE = 3;
    protected static final long DEFAULT_STALE_REQUEST_LIMIT = 30000; // milliseconds
    protected static final int DEFAULT_TIME_PRIORITY_GRANULARITY = 500; // milliseconds

    protected final String RUNNING_THREAD_NAME_PREFIX = Logging.getMessage(
        "BasicRetrievalService.RunningThreadNamePrefix");
    protected static final String IDLE_THREAD_NAME_PREFIX = Logging.getMessage(
        "BasicRetrievalService.IdleThreadNamePrefix");

    protected RetrievalExecutor executor; // thread pool for running retrievers
    protected ConcurrentLinkedQueue<RetrievalTask> activeTasks; // tasks currently allocated a thread
    protected int queueSize; // maximum queue size

    /** Encapsulates a single threaded retrieval as a {@link java.util.concurrent.FutureTask}. */
    protected static class RetrievalTask extends FutureTask<Retriever>
        implements RetrievalFuture, Comparable<RetrievalTask>
    {
        protected Retriever retriever;
        protected double priority; // retrieval secondary priority (primary priority is submit time)

        protected RetrievalTask(Retriever retriever, double priority)
        {
            super(retriever);
            this.retriever = retriever;
            this.priority = priority;
        }

        public double getPriority()
        {
            return priority;
        }

        public Retriever getRetriever()
        {
            return this.retriever;
        }

        @Override
        public void run()
        {
            if (this.isDone() || this.isCancelled())
                return;

            super.run();
        }

        /**
         * @param that the task to compare with this one
         *
         * @return 0 if task priorities are equal, -1 if priority of this is less than that, 1 otherwise
         *
         * @throws IllegalArgumentException if <code>that</code> is null
         */
        public int compareTo(RetrievalTask that)
        {
            if (that == null)
            {
                String msg = Logging.getMessage("nullValue.RetrieverIsNull");
                Logging.verbose(msg);
                throw new IllegalArgumentException(msg);
            }

            if (this.priority > 0 && that.priority > 0) // only secondary priority used if either is negative
            {
                // Requests submitted within different time-granularity periods are ordered exclusive of their
                // client-specified priority.
                long now = System.currentTimeMillis();
                long thisElapsedTime = now - this.retriever.getSubmitTime();
                long thatElapsedTime = now - that.retriever.getSubmitTime();
                if (((thisElapsedTime - thatElapsedTime) / DEFAULT_TIME_PRIORITY_GRANULARITY) != 0)
                    return thisElapsedTime < thatElapsedTime ? -1 : 1;
            }

            // The client-specified priority is compared for requests submitted within the same granularity period.
            return this.priority == that.priority ? 0 : this.priority < that.priority ? -1 : 1;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            final RetrievalTask that = (RetrievalTask) o;

            // Tasks are equal if their retrievers are equivalent
            return this.retriever.equals(that.retriever);
            // Priority and submit time are not factors in equality
        }

        @Override
        public int hashCode()
        {
            return this.retriever.getName().hashCode();
        }
    }

    public void uncaughtException(Thread thread, Throwable throwable)
    {
        Logging.verbose(Logging.getMessage("BasicRetrievalService.UncaughtExceptionDuringRetrieval",
            thread.getName()));
    }

    protected class RetrievalExecutor extends ThreadPoolExecutor
    {
        protected static final long THREAD_TIMEOUT = 2; // keep idle threads alive this many seconds
        protected long staleRequestLimit; // reject requests older than this

        protected RetrievalExecutor(int poolSize, int queueSize)
        {
            super(poolSize, poolSize, THREAD_TIMEOUT, TimeUnit.SECONDS, new PriorityBlockingQueue<Runnable>(queueSize),
                new ThreadFactory()
                {
                    public Thread newThread(Runnable runnable)
                    {
                        Thread thread = new Thread(runnable);
                        thread.setDaemon(true);
                        thread.setPriority(Thread.MIN_PRIORITY);
                        thread.setUncaughtExceptionHandler(BasicRetrievalService.this);
                        return thread;
                    }
                }, new ThreadPoolExecutor.DiscardPolicy() // abandon task when queue is full
            {
                // This listener is invoked only when the executor queue is a bounded queue and runs out of room.
                // If the queue is a java.util.concurrent.PriorityBlockingQueue, this listener is never invoked.
                @Override
                public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor)
                {
                    // Interposes logging for rejected execution
                    Logging.verbose(Logging.getMessage("BasicRetrievalService.ResourceRejected",
                        ((RetrievalTask) runnable).getRetriever().getName()));

                    super.rejectedExecution(runnable, threadPoolExecutor);
                }
            }
            );

            this.staleRequestLimit = Configuration.getLongValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT,
                DEFAULT_STALE_REQUEST_LIMIT);
        }

        /**
         * @param thread   the thread the task is running on
         * @param runnable the <code>Retriever</code> running on the thread
         *
         * @throws IllegalArgumentException if either <code>thread</code> or <code>runnable</code> is null
         */
        @Override
        protected void beforeExecute(Thread thread, Runnable runnable)
        {
            if (thread == null)
            {
                String msg = Logging.getMessage("nullValue.ThreadIsNull");
                Logging.verbose(msg);
                throw new IllegalArgumentException(msg);
            }
            if (runnable == null)
            {
                String msg = Logging.getMessage("nullValue.RunnableIsNull");
                Logging.verbose(msg);
                throw new IllegalArgumentException(msg);
            }

            RetrievalTask task = (RetrievalTask) runnable;

            task.retriever.setBeginTime(System.currentTimeMillis());
            long limit = task.retriever.getStaleRequestLimit() >= 0
                ? task.retriever.getStaleRequestLimit() : this.staleRequestLimit;
            if (task.retriever.getBeginTime() - task.retriever.getSubmitTime() > limit)
            {
                // Task has been sitting on the queue too long
                Logging.verbose(Logging.getMessage("BasicRetrievalService.CancellingTooOldRetrieval",
                    task.getRetriever().getName()));
                task.cancel(true);
            }

            if (BasicRetrievalService.this.activeTasks.contains(task))
            {
                // Task is a duplicate
                Logging.verbose(Logging.getMessage("BasicRetrievalService.CancellingDuplicateRetrieval",
                    task.getRetriever().getName()));
                task.cancel(true);
            }

            BasicRetrievalService.this.activeTasks.add(task);

            thread.setName(RUNNING_THREAD_NAME_PREFIX + task.getRetriever().getName());
            thread.setPriority(Thread.MIN_PRIORITY); // Subordinate thread priority to rendering
            thread.setUncaughtExceptionHandler(BasicRetrievalService.this);

            super.beforeExecute(thread, runnable);
        }

        /**
         * @param runnable  the <code>Retriever</code> running on the thread
         * @param throwable an exception thrown during retrieval, will be null if no exception occurred
         *
         * @throws IllegalArgumentException if <code>runnable</code> is null
         */
        protected void afterExecute(Runnable runnable, Throwable throwable)
        {
            if (runnable == null)
            {
                String msg = Logging.getMessage("nullValue.RunnableIsNull");
                Logging.verbose(msg);
                throw new IllegalArgumentException(msg);
            }

            super.afterExecute(runnable, throwable);

            RetrievalTask task = (RetrievalTask) runnable;
            BasicRetrievalService.this.activeTasks.remove(task);
            task.retriever.setEndTime(System.currentTimeMillis());

            try
            {
                if (throwable != null)
                {
                    Logging.verbose(Logging.getMessage("BasicRetrievalService.ExceptionDuringRetrieval",
                        task.getRetriever().getName()), throwable);
                }

                task.get(); // Wait for task to finish, cancel or break
            }
            catch (java.util.concurrent.ExecutionException e)
            {
                String message = Logging.getMessage("BasicRetrievalService.ExecutionExceptionDuringRetrieval",
                    task.getRetriever().getName());
                if (e.getCause() instanceof SocketTimeoutException)
                {
                    Logging.verbose(message + " " + e.getCause().getLocalizedMessage());
                }
                else
                {
                    Logging.verbose(message, e);
                }
            }
            catch (InterruptedException e)
            {
                Logging.verbose(Logging.getMessage("BasicRetrievalService.RetrievalInterrupted",
                    task.getRetriever().getName()), e);
            }
            catch (java.util.concurrent.CancellationException e)
            {
                Logging.verbose(Logging.getMessage("BasicRetrievalService.RetrievalCancelled",
                    task.getRetriever().getName()));
            }
            finally
            {
                Thread.currentThread().setName(IDLE_THREAD_NAME_PREFIX);
            }
        }
    }

    public BasicRetrievalService()
    {
        Integer poolSize = Configuration.getIntegerValue(AVKey.RETRIEVAL_POOL_SIZE, DEFAULT_POOL_SIZE);
        this.queueSize = Configuration.getIntegerValue(AVKey.RETRIEVAL_QUEUE_SIZE, DEFAULT_QUEUE_SIZE);

        // this.executor runs the retrievers, each in their own thread
        this.executor = new RetrievalExecutor(poolSize, this.queueSize);

        // this.activeTasks holds the list of currently executing tasks (*not* those pending on the queue)
        this.activeTasks = new ConcurrentLinkedQueue<RetrievalTask>();
    }

    public void shutdown(boolean immediately)
    {
        if (immediately)
            this.executor.shutdownNow();
        else
            this.executor.shutdown();

        this.activeTasks.clear();
    }

    /** {@inheritDoc} */
    public RetrievalFuture runRetriever(Retriever retriever)
    {
        if (retriever == null)
        {
            String msg = Logging.getMessage("nullValue.RetrieverIsNull");
            Logging.verbose(msg);
            throw new IllegalArgumentException(msg);
        }
        if (retriever.getName() == null)
        {
            String message = Logging.getMessage("nullValue.RetrieverNameIsNull");
            Logging.verbose(message);
            throw new IllegalArgumentException(message);
        }

        // Add with secondary priority that removes most recently added requests first.
        return this.runRetriever(retriever, (double) (Long.MAX_VALUE - System.currentTimeMillis()));
    }

    /** {@inheritDoc} */
    public synchronized RetrievalFuture runRetriever(Retriever retriever, double priority)
    {
        if (retriever == null)
        {
            String message = Logging.getMessage("nullValue.RetrieverIsNull");
            Logging.verbose(message);
            throw new IllegalArgumentException(message);
        }

        if (retriever.getName() == null)
        {
            String message = Logging.getMessage("nullValue.RetrieverNameIsNull");
            Logging.verbose(message);
            throw new IllegalArgumentException(message);
        }

        if (!this.isAvailable())
        {
            Logging.verbose(Logging.getMessage("BasicRetrievalService.ResourceRejected", retriever.getName()));
        }

        RetrievalTask task = new RetrievalTask(retriever, priority);
        retriever.setSubmitTime(System.currentTimeMillis());

        // Do not queue duplicates.
        if (this.activeTasks.contains(task) || this.executor.getQueue().contains(task))
            return null;

        this.executor.execute(task);

        return task;
    }

    /** {@inheritDoc} */
    public void setRetrieverPoolSize(int poolSize)
    {
        if (poolSize < 1)
        {
            String message = Logging.getMessage("BasicRetrievalService.RetrieverPoolSizeIsLessThanOne");
            Logging.verbose(message);
            throw new IllegalArgumentException(message);
        }

        this.executor.setCorePoolSize(poolSize);
        this.executor.setMaximumPoolSize(poolSize);
    }

    /** {@inheritDoc} */
    public int getRetrieverPoolSize()
    {
        return this.executor.getCorePoolSize();
    }

    protected boolean hasRetrievers()
    {
        Thread[] threads = new Thread[Thread.activeCount()];
        int numThreads = Thread.enumerate(threads);
        for (int i = 0; i < numThreads; i++)
        {
            if (threads[i].getName().startsWith(RUNNING_THREAD_NAME_PREFIX))
                return true;
        }
        return false;
    }

    /** {@inheritDoc} */
    public boolean hasActiveTasks()
    {
        return this.hasRetrievers();
    }

    /** {@inheritDoc} */
    public boolean isAvailable()
    {
        return this.executor.getQueue().size() < this.queueSize;
    }

    /** {@inheritDoc} */
    public int getNumRetrieversPending()
    {
        // Could use same method to determine active tasks as hasRetrievers() above, but this method only advisory.
        return this.activeTasks.size() + this.executor.getQueue().size();
    }

    /** {@inheritDoc} */
    public boolean contains(Retriever retriever)
    {
        if (retriever == null)
        {
            String msg = Logging.getMessage("nullValue.RetrieverIsNull");
            Logging.verbose(msg);
            throw new IllegalArgumentException(msg);
        }
        RetrievalTask task = new RetrievalTask(retriever, 0d);
        return (this.activeTasks.contains(task) || this.executor.getQueue().contains(task));
    }
}
