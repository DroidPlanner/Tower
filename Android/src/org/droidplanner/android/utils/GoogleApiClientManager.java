package org.droidplanner.android.utils;

import java.util.concurrent.LinkedBlockingQueue;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Handles the lifecycle for the google api client. Also takes care of running
 * submitted tasks when the google api client is connected.
 */
public class GoogleApiClientManager {

	private final static String TAG = GoogleApiClientManager.class.getSimpleName();

	/**
	 * Manager background thread used to run the submitted google api client
	 * tasks.
	 */
	private final Runnable mDriverRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				while (true) {
					final GoogleApiClientTask task = mTaskQueue.take();

					if (!mGoogleApiClient.isConnected()) {
						final ConnectionResult result = mGoogleApiClient.blockingConnect();
						if (!result.isSuccess()) {
							throw new IllegalStateException("Unable to connect to the google api "
									+ "client: " + result.getErrorCode());
						}
					}

					if (task.mRunOnBackgroundThread) {
						mBgHandler.post(task);
					} else {
						mMainHandler.post(task);
					}
				}
			} catch (InterruptedException e) {
				Log.v(TAG, e.getMessage(), e);
			}
		}
	};

	private Thread mDriverThread;

	/**
	 * This handler is in charge of running google api client tasks on the
	 * calling thread.
	 */
	private final Handler mMainHandler;

	/**
	 * This handler is in charge of running google api client tasks on the
	 * background thread.
	 */
	private Handler mBgHandler;
	private HandlerThread mBgHandlerThread;

	/**
	 * Application context.
	 */
	private final Context mContext;

	/**
	 * Handle to the google api client.
	 */
	private final GoogleApiClient mGoogleApiClient;

	/**
	 * Holds tasks that needs to be run using the google api client. A
	 * background thread will be blocking on this queue until new tasks are
	 * inserted. In which case, it will retrieve the new task, and process it.
	 */
	private final LinkedBlockingQueue<GoogleApiClientTask> mTaskQueue = new LinkedBlockingQueue<GoogleApiClientTask>();

	public GoogleApiClientManager(Context context,
			Api<? extends Api.ApiOptions.NotRequiredOptions>... apis) {
		mContext = context;
		mMainHandler = new Handler();

		final GoogleApiClient.Builder apiBuilder = new GoogleApiClient.Builder(context);
		for (Api api : apis) {
			apiBuilder.addApi(api);
		}

		mGoogleApiClient = apiBuilder.build();
	}

	private void destroyBgHandler() {
		if (mBgHandlerThread != null && mBgHandlerThread.isAlive()) {
			mBgHandlerThread.quit();
			mBgHandlerThread.interrupt();
			mBgHandlerThread = null;
		}

		mBgHandler = null;
	}

	private void destroyDriverThread() {
		if (mDriverThread != null && mDriverThread.isAlive()) {
			mDriverThread.interrupt();
			mDriverThread = null;
		}
	}

	private void initializeBgHandler() {
		if (mBgHandlerThread == null || mBgHandlerThread.isInterrupted()) {
			mBgHandlerThread = new HandlerThread("GAC Manager Background Thread");
			mBgHandlerThread.start();
			mBgHandler = null;
		}

		if (mBgHandler == null) {
			mBgHandler = new Handler(mBgHandlerThread.getLooper());
		}
	}

	private void initializeDriverThread() {
		if (mDriverThread == null || mDriverThread.isInterrupted()) {
			mDriverThread = new Thread(mDriverRunnable, "GAC Manager Driver Thread");
			mDriverThread.start();
		}
	}

	/**
	 * Adds a task to the google api client manager tasks queue. This task will
	 * be scheduled to run on the calling thread.
	 * 
	 * @param task
	 *            task making use of the google api client.
	 * @return true if the task was successfully added to the queue.
	 * @throws IllegalStateException
	 *             is the start() method was not called.
	 */
	public boolean addTask(GoogleApiClientTask task) {
		if (!isStarted()) {
			throw new IllegalStateException("GoogleApiClientManager#start() was not called.");
		}

		task.mRunOnBackgroundThread = false;
		return mTaskQueue.offer(task);
	}

	/**
	 * Adds a task to the google api client manager tasks queue. This task will
	 * be scheduled to run on a background thread.
	 * 
	 * @param task
	 *            task making use of the google api client.
	 * @return true if the task was successfully added to the queue.
	 * @throws IllegalStateException
	 *             is the start() method was not called.
	 */
	public boolean addTaskToBackground(GoogleApiClientTask task) {
		if (!isStarted()) {
			throw new IllegalStateException("GoogleApiClientManager#start() was not called.");
		}

		task.mRunOnBackgroundThread = true;
		return mTaskQueue.offer(task);
	}

	/**
	 * @return true the google api client manager was started.
	 */
	private boolean isStarted() {
		return mDriverThread != null && mDriverThread.isAlive() && mBgHandlerThread != null
				&& mBgHandlerThread.isAlive() && mBgHandler != null
				&& mBgHandler.getLooper() != null;
	}

	/**
	 * Activates the google api client manager.
	 */
	public void start() {
		initializeDriverThread();
		initializeBgHandler();
	}

	/**
	 * Release the resources used by this manager. After calling this method,
	 * start() needs to be called again to use that manager again.
	 */
	public void stop() {
		destroyBgHandler();
		destroyDriverThread();

		mTaskQueue.clear();
		if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()) {
			mGoogleApiClient.disconnect();
		}
	}

	/**
	 * Type for the google api client tasks.
	 */
	public abstract class GoogleApiClientTask implements Runnable {

		/**
		 * If true, this task will be scheduled to run on a background thread.
		 * Otherwise, it will run on the calling thread.
		 */
		private boolean mRunOnBackgroundThread = false;

		protected GoogleApiClient getGoogleApiClient() {
			return mGoogleApiClient;
		}

		@Override
		public void run() {
			if (!getGoogleApiClient().isConnected()) {
				// Add the task back to the queue.
				mTaskQueue.offer(this);
				return;
			}

			// Run the task
			doRun();
		}

		protected abstract void doRun();

	}
}
