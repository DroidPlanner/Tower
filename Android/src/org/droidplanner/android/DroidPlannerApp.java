package org.droidplanner.android;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;

import com.ox3dr.services.android.lib.model.IDroidPlannerServices;
import com.ox3dr.services.android.lib.model.ITLogApi;

import org.droidplanner.android.api.Drone;
import org.droidplanner.android.notifications.NotificationHandler;
import org.droidplanner.android.utils.analytics.GAUtils;
import org.droidplanner.android.utils.file.IO.ExceptionWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DroidPlannerApp extends Application {

	private static final long DELAY_TO_DISCONNECTION = 60000l; // ms

    private static final String CLAZZ_NAME = DroidPlannerApp.class.getName();
	private static final String TAG = DroidPlannerApp.class.getSimpleName();

	private static final int API_UNBOUND = 0;
	private static final int API_BOUND = 1;

    public static final String ACTION_TOGGLE_DRONE_CONNECTION = CLAZZ_NAME +
            ".ACTION_TOGGLE_DRONE_CONNECTION";
    public static final String EXTRA_ESTABLISH_CONNECTION = "extra_establish_connection";

    private final static IntentFilter intentFilter = new IntentFilter
            (ACTION_TOGGLE_DRONE_CONNECTION);

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(ACTION_TOGGLE_DRONE_CONNECTION.equals(action)){
                boolean connect = intent.getBooleanExtra(EXTRA_ESTABLISH_CONNECTION,
                        !drone.isConnected());
                if(connect)
                    drone.connect();
                else
                    drone.disconnect();
            }
        }
    };

    public interface ApiListener {
		void onApiConnected();

		void onApiDisconnected();
	}

	private final AtomicInteger apiBindingState = new AtomicInteger(API_UNBOUND);

	private final ServiceConnection ox3drServicesConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			ox3drServices = IDroidPlannerServices.Stub.asInterface(service);

            drone.start();
            notificationHandler = new NotificationHandler(getApplicationContext(), drone);
            notifyApiConnected();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
            disconnect();
		}
	};

	private final Runnable disconnectionTask = new Runnable() {
		@Override
		public void run() {
			disconnect();
		}
	};

	private final Handler handler = new Handler();
	private final List<ApiListener> apiListeners = new ArrayList<ApiListener>();

	private final Thread.UncaughtExceptionHandler dpExceptionHandler = new Thread.UncaughtExceptionHandler() {
		@Override
		public void uncaughtException(Thread thread, Throwable ex) {
			new ExceptionWriter(ex).saveStackTraceToSD();
			exceptionHandler.uncaughtException(thread, ex);
		}
	};

	private Thread.UncaughtExceptionHandler exceptionHandler;

    private Drone drone;
	private IDroidPlannerServices ox3drServices;
    private ITLogApi tlogApi;
    private NotificationHandler notificationHandler;

	@Override
	public void onCreate() {
		super.onCreate();
		final Context context = getApplicationContext();

        drone = new Drone(this);

		exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(dpExceptionHandler);

		GAUtils.initGATracker(this);
		GAUtils.startNewSession(context);

        registerReceiver(broadcastReceiver, intentFilter);
	}

	public Drone getDrone(){
        return drone;
    }

    public ITLogApi getTlogApi(){
        if(tlogApi == null){
            try {
                tlogApi = ox3drServices.getTLogApi();
            } catch (RemoteException e) {
                return null;
            }
        }

        return tlogApi;
    }

    public IDroidPlannerServices get3drServices(){
        return ox3drServices;
    }

	public void addApiListener(ApiListener listener) {
		if (listener == null)
			return;

		if (is3drServicesConnected())
            listener.onApiConnected();

		apiListeners.add(listener);

		handler.removeCallbacks(disconnectionTask);
		connect();
	}

	public void removeApiListener(ApiListener listener) {
		if (listener != null) {
			apiListeners.remove(listener);
			listener.onApiDisconnected();
		}

		if (apiListeners.isEmpty() && apiBindingState.get() != API_UNBOUND) {
            // Wait a minute, then disconnect the service binding.
            handler.postDelayed(disconnectionTask, DELAY_TO_DISCONNECTION);
		}
	}

	private void notifyApiConnected() {
		if (apiListeners.isEmpty() || !is3drServicesConnected())
			return;

			for (ApiListener listener : apiListeners)
				listener.onApiConnected();
	}

	private void notifyApiDisconnected() {
		if (apiListeners.isEmpty())
			return;

		for (ApiListener listener : apiListeners)
			listener.onApiDisconnected();
	}

	public boolean is3drServicesConnected() {
		return ox3drServices != null;
	}

    public void connect() {
        if (apiBindingState.compareAndSet(API_UNBOUND, API_BOUND)) {
            bindService(new Intent(IDroidPlannerServices.class.getName()), ox3drServicesConnection,
                    Context.BIND_AUTO_CREATE);
        }
    }

    public void disconnect(){
        if (apiBindingState.compareAndSet(API_BOUND, API_UNBOUND)) {
            notifyApiDisconnected();
            notificationHandler.terminate();
            drone.terminate();

            ox3drServices = null;
            unbindService(ox3drServicesConnection);
        }
    }
}
