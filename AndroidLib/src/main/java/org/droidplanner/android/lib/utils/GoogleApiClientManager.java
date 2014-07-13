package org.droidplanner.android.lib.utils;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Handles the lifecycle for the google api client. Also takes care of running submitted tasks
 * when the google api client is connected.
 */
public class GoogleApiClientManager {

    private final static String TAG = GoogleApiClientManager.class.getSimpleName();
    private final static boolean DEFAULT_DISCONNECT_ON_TASKS_COMPLETION = true;

    /**
     * Manager background thread used to run the submitted google api client tasks.
     */
    private final Thread mBgThread = new Thread("GAC Manager Background Thread"){

        @Override
        public void run() {
            try{
                while(!isInterrupted()){
                    final GoogleApiClientTask task = mTaskQueue.take();

                    if(!mGoogleApiClient.isConnected()){
                        final ConnectionResult result = mGoogleApiClient.blockingConnect();
                        if(!result.isSuccess()){
                            throw new IllegalStateException("Unable to connect to the google api " +
                                    "client: " + result.getErrorCode());
                        }
                    }

                    task.run();

                    if(mTaskQueue.size() == 0 && mDisconnectOnTasksCompletion){
                        mGoogleApiClient.disconnect();
                    }
                }

                if(mDisconnectOnTasksCompletion){
                    mGoogleApiClient.disconnect();
                }
            }
            catch(InterruptedException e){
                Log.w(TAG, e.getMessage(), e);
            }
        }
    };

    /**
     * Application context.
     */
    private final Context mContext;

    /**
     * Handle to the google api client.
     */
    private final GoogleApiClient mGoogleApiClient;

    /**
     * If true, the google api client will be disconnected when the tasks are complete,
     * until new tasks are ready for processing.
     */
    private boolean mDisconnectOnTasksCompletion = DEFAULT_DISCONNECT_ON_TASKS_COMPLETION;

    /**
     * Holds tasks that needs to be run using the google api client.
     * A background thread will be blocking on this queue until new tasks are inserted. In which
     * case, it will retrieve the new task, and process it.
     */
    private final LinkedBlockingQueue<GoogleApiClientTask> mTaskQueue = new LinkedBlockingQueue
            <GoogleApiClientTask>();

    public GoogleApiClientManager(Context context, Api<? extends Api.ApiOptions.NotRequiredOptions>
            ... apis){
        mContext = context;

        final GoogleApiClient.Builder apiBuilder = new GoogleApiClient.Builder(context);
        for(Api api: apis){
            apiBuilder.addApi(api);
        }

        mGoogleApiClient = apiBuilder.build();
    }

    /**
     * Adds a task to the google api client manager tasks queue.
     * @param task
     * @return true if the task was successfully added to the queue.
     * @throws IllegalStateException is the start() method was not called.
     */
    public boolean addTask(GoogleApiClientTask task){
        if(!isStarted()){
            throw new IllegalStateException("GoogleApiClientManager#start() was not called.");
        }

        return mTaskQueue.offer(task);
    }

    /**
     * @return true the google api client manager was started.
     */
    private boolean isStarted(){
        return mBgThread.isAlive();
    }

    /**
     * Activates the google api client manager.
     */
    public void start(){
        start(DEFAULT_DISCONNECT_ON_TASKS_COMPLETION);
    }

    /**
     * Activates the google api client manager.
     * @param disconnectOnTasksCompletion true to disconnect the google api client on tasks
     *                                    completion.
     */
    public void start(boolean disconnectOnTasksCompletion){
        mDisconnectOnTasksCompletion = disconnectOnTasksCompletion;

        if(!mBgThread.isAlive()){
            mBgThread.start();
        }
    }

    /**
     * Release the resources used by this manager.
     * After calling this method, start() needs to be called again to use that manager again.
     */
    public void terminate(){
        if(mBgThread.isAlive()){
            mBgThread.interrupt();
        }

        mTaskQueue.clear();
        if(mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()){
            mGoogleApiClient.disconnect();
        }

        mDisconnectOnTasksCompletion = DEFAULT_DISCONNECT_ON_TASKS_COMPLETION;
    }

    /**
     * Type for the google api client tasks.
     */
    public abstract class GoogleApiClientTask implements Runnable {

        protected GoogleApiClient getGoogleApiClient(){
            return mGoogleApiClient;
        }

    }
}
