/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.util;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.avlist.AVKey;

import java.util.Queue;
import java.util.concurrent.*;

/**
 * @author dcollins
 * @version $Id$
 */
public class BasicTaskService extends WWObjectImpl implements TaskService, Thread.UncaughtExceptionHandler
{
    protected static class TaskExecutor extends ThreadPoolExecutor
    {
        protected Queue<Runnable> activeQueue;

        /**
         * Creates a TaskExecutor with a fixed thread pool size and a maximum task queue size. The created executor
         * creates daemon threads in a thread pool with size of poolSize, keeps idle threads alive for
         * <code>DEFAULT_THREAD_TIMEOUT</code> seconds, has a maximum waiting queue size of queueSize, and forwards all
         * uncaught exceptions to the uncaughtExceptionHandler.
         * <p/>
         * The threads are created as daemons to enable the JVM to shut down even when this task service has active
         * tasks. Uncaught task exceptions are forwarded to the uncaughtExceptionHandler. This exception handler must
         * not forward the exception to the ThreadGroup's uncaughtException method, as this causes the JVM to terminate
         * with a FATAL EXCEPTION.
         *
         * @param poolSize                 the number of threads in the pool.
         * @param queueSize                the maximum size of the task queue.
         * @param uncaughtExceptionHandler the handler to invoke when a task terminates due to an uncaught exception.
         */
        public TaskExecutor(int poolSize, int queueSize, final Thread.UncaughtExceptionHandler uncaughtExceptionHandler)
        {
            // Create an executor with a core pool size and a maximum pool size of poolSize, where each thread is
            // configured to timeout after two seconds. If the core pool size is less than the maximum pool size, this
            // executor lets tasks queue up before creating additional threads.
            super(poolSize, poolSize, DEFAULT_THREAD_TIMEOUT, TimeUnit.SECONDS,
                // Create a bounded synchronized task with the specified maximum size.
                new ArrayBlockingQueue<Runnable>(queueSize),
                // Use a thread factory that creates daemon threads configured with the specified
                // UncaughtExceptionHandler.
                new ThreadFactory()
                {
                    public Thread newThread(Runnable runnable)
                    {
                        Thread thread = new Thread(runnable);
                        thread.setDaemon(true); // Daemon threads don't prevent the JVM from shutting down.
                        thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
                        return thread;
                    }
                },
                // Use a rejected execution handler that discards tasks that cannot be executed either because the
                // service is shutdown or the task queue is full.
                new ThreadPoolExecutor.DiscardPolicy()
                {
                    public void rejectedExecution(Runnable runnable,
                        ThreadPoolExecutor threadPoolExecutor)
                    {
                        // Interposes logging for rejected execution
                        String msg = Logging.getMessage("TaskService.TaskRejected", runnable);
                        Logging.info(msg);
                        super.rejectedExecution(runnable, threadPoolExecutor);
                    }
                });

            // Since we've configured the core pool size to a value greater than zero, we must configure this executor
            // to timeout those core threads. Otherwise this executor's threads are never cleaned up once they're
            // created.
            this.allowCoreThreadTimeOut(true);
            // Create a queue used to track which tasks are currently running.
            this.activeQueue = new ConcurrentLinkedQueue<Runnable>();
        }

        public Queue<Runnable> getActiveQueue()
        {
            return this.activeQueue;
        }

        public boolean contains(Runnable command)
        {
            return this.activeQueue.contains(command) || this.getQueue().contains(command);
        }

        /**
         * {@inheritDoc}
         * <p/>
         * Overridden to discard duplicate tasks, add the specified task to this TaskExecutor's
         * <code>activeQueue</code>, and set the thread's name to include the World Wind running thread name prefix.
         */
        protected void beforeExecute(Thread thread, Runnable runnable)
        {
            if (this.activeQueue.contains(runnable))
            {
                // Duplicate requests are simply interrupted here. The task itself must check the thread's isInterrupted
                // flag and actually terminate the task.
                String message = Logging.getMessage("TaskService.CancellingDuplicateTask", runnable);
                Logging.info(message);
                thread.interrupt();
                return;
            }

            this.activeQueue.add(runnable);

            if (!WWUtil.isEmpty(RUNNING_THREAD_NAME_PREFIX))
                thread.setName(RUNNING_THREAD_NAME_PREFIX + ": " + runnable);

            // Invoke super.beforeExecute at the end of this method to properly nest multiple overridings.
            super.beforeExecute(thread, runnable);
        }

        /**
         * {@inheritDoc}
         * <p/>
         * Overridden to remove the specified task to this TaskExecutor's <code>activeQueue</code>, and set the thread's
         * name to include the World Wind idle thread name prefix.
         */
        protected void afterExecute(Runnable runnable, Throwable throwable)
        {
            // Invoke super.afterExecute at the beginning of this method to properly nest multiple overridings.
            super.afterExecute(runnable, throwable);

            this.activeQueue.remove(runnable);

            if (throwable == null && !WWUtil.isEmpty(IDLE_THREAD_NAME_PREFIX))
                Thread.currentThread().setName(IDLE_THREAD_NAME_PREFIX); // Guaranteed
        }
    }

    protected static final String RUNNING_THREAD_NAME_PREFIX = Logging.getMessage(
        "TaskService.RunningThreadNamePrefix");
    protected static final String IDLE_THREAD_NAME_PREFIX = Logging.getMessage(
        "TaskService.IdleThreadNamePrefix");
    /** The default number of seconds that idle threads are kept alive: 2 seconds. */
    protected static final long DEFAULT_THREAD_TIMEOUT = 2;

    protected TaskExecutor executor;

    public BasicTaskService()
    {
        this.executor = this.createExecutor();
    }

    protected TaskExecutor createExecutor()
    {
        Integer poolSize = Configuration.getIntegerValue(AVKey.TASK_SERVICE_POOL_SIZE);
        Integer queueSize = Configuration.getIntegerValue(AVKey.TASK_SERVICE_QUEUE_SIZE);

        return new TaskExecutor(poolSize, queueSize, this);
    }

    /** {@inheritDoc} */
    public synchronized void runTask(Runnable task)
    {
        if (task == null)
        {
            String msg = Logging.getMessage("nullValue.TaskIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // Silently ignore duplicate tasks.
        if (this.executor.contains(task))
            return;

        this.executor.execute(task);
    }

    /** {@inheritDoc} */
    public synchronized boolean contains(Runnable task)
    {
        if (task == null)
        {
            String msg = Logging.getMessage("nullValue.TaskIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.executor.contains(task);
    }

    /** {@inheritDoc} */
    public synchronized boolean isFull()
    {
        return this.executor.getQueue().remainingCapacity() == 0;
    }

    public synchronized void uncaughtException(Thread thread, Throwable throwable)
    {
        // Just log the uncaught exception and return without doing anything. In this case the TaskExecutor's
        // afterExecute method still successfully cleans up after this thread. The JVM terminates with a FATAL EXCEPTION
        // if we delegate this exception to the ThreadGroup's uncaughtException method.
        String msg = Logging.getMessage("TaskService.UncaughtTaskException", thread.getName());
        Logging.info(msg, throwable);
    }
}
