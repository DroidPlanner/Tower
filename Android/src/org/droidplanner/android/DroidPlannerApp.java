package org.droidplanner.android;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.ox3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.ox3dr.services.android.lib.drone.connection.ConnectionType;
import com.ox3dr.services.android.lib.model.IDroidPlannerServices;
import com.ox3dr.services.android.lib.model.ITLogApi;

import org.droidplanner.android.activities.helpers.BluetoothDevicesActivity;
import org.droidplanner.android.api.DPApiCallback;
import org.droidplanner.android.api.DroneApi;
import org.droidplanner.android.notifications.NotificationHandler;
import org.droidplanner.android.utils.analytics.GAUtils;
import org.droidplanner.android.utils.file.IO.ExceptionWriter;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DroidPlannerApp extends Application {

	private static final long DELAY_TO_DISCONNECTION = 60000l; // ms

	private static final String TAG = DroidPlannerApp.class.getSimpleName();

	private static final int API_UNBOUND = 0;
	private static final int API_BOUND = 1;

    public void reconnect() {
        if (apiBindingState.compareAndSet(API_UNBOUND, API_BOUND)) {
            bindService(new Intent(IDroidPlannerServices.class.getName()), ox3drServicesConnection,
                    Context.BIND_AUTO_CREATE);
        }
    }

    public interface ApiListener {
		void onApiConnected();

		void onApiDisconnected();
	}

	private final AtomicInteger apiBindingState = new AtomicInteger(API_UNBOUND);

	private final ServiceConnection ox3drServicesConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			ox3drServices = IDroidPlannerServices.Stub.asInterface(service);
            notifyApiConnected();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
            apiBindingState.set(API_UNBOUND);
			notifyApiDisconnected();
			ox3drServices = null;
		}
	};

	private final Runnable disconnectionTask = new Runnable() {
		@Override
		public void run() {
			if (apiBindingState.compareAndSet(API_BOUND, API_UNBOUND)) {
				notifyApiDisconnected();
				unbindService(ox3drServicesConnection);
			}
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

    private DroneApi droneApi;

	private IDroidPlannerServices ox3drServices;
    private ITLogApi tlogApi;

    private NotificationHandler notificationHandler;

	@Override
	public void onCreate() {
		super.onCreate();
		final Context context = getApplicationContext();

        droneApi = new DroneApi(this);

        notificationHandler = new NotificationHandler(context, droneApi);

		exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(dpExceptionHandler);

		GAUtils.initGATracker(this);
		GAUtils.startNewSession(context);
	}

	public DroneApi getDroneApi(){
        return droneApi;
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
		reconnect();
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
}
