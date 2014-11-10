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

	public interface ApiListener {
		void onApiConnected();

		void onApiDisconnected();
	}

	private final AtomicInteger apiBindingState = new AtomicInteger(API_UNBOUND);
	private DPApiCallback dpCallback;

	private final ServiceConnection ox3drServicesConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			ox3drServices = IDroidPlannerServices.Stub.asInterface(service);
            registerWithDrone();
            notifyApiConnected();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			notifyApiDisconnected();
			ox3drServices = null;
            unregisterFromDrone();
		}
	};

	private final Runnable disconnectionTask = new Runnable() {
		@Override
		public void run() {
			if (apiBindingState.compareAndSet(API_BOUND, API_UNBOUND)) {
				notifyApiDisconnected();
                unregisterFromDrone();
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

	private DroidPlannerPrefs dpPrefs;
    private NotificationHandler notificationHandler;

	@Override
	public void onCreate() {
		super.onCreate();
		final Context context = getApplicationContext();

        dpCallback = new DPApiCallback(this);
        droneApi = new DroneApi(context);
		dpPrefs = new DroidPlannerPrefs(context);
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

	public void addApiListener(ApiListener listener) {
		if (listener == null)
			return;

		if (is3drServicesConnected())
            listener.onApiConnected();

		apiListeners.add(listener);

		handler.removeCallbacks(disconnectionTask);
		if (apiBindingState.compareAndSet(API_UNBOUND, API_BOUND)) {
			bindService(new Intent(IDroidPlannerServices.class.getName()), ox3drServicesConnection,
					Context.BIND_AUTO_CREATE);
		}
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

	private void registerWithDrone() {
		if (!is3drServicesConnected() || droneApi.isConnected())
			return;

		// Retrieve the connection parameters.
		final ConnectionParameter connParams = retrieveConnectionParameters();
		if (connParams == null) {
			Log.e(TAG, "Invalid connection parameters");
			return;
		}

		try {
            droneApi.setDpApi(ox3drServices.registerWithDrone(connParams, dpCallback));
		} catch (RemoteException e) {
			Log.e(TAG, "Unable to retrieve a droidplanner api connection.", e);
		}
	}

	private void unregisterFromDrone() {
        droneApi.setDpApi(null);

		if (!is3drServicesConnected())
			return; // Nothing to do. It's already disconnected.

		try {
			ox3drServices.unregisterFromDrone(retrieveConnectionParameters(), dpCallback);
		} catch (RemoteException e) {
			Log.e(TAG, "Error while disconnecting from the droidplanner api", e);
		}
	}

	private boolean is3drServicesConnected() {
		return ox3drServices != null;
	}

	private ConnectionParameter retrieveConnectionParameters() {
		final int connectionType = dpPrefs.getConnectionParameterType();
		Bundle extraParams = new Bundle();

		ConnectionParameter connParams;
		switch (connectionType) {
		case ConnectionType.TYPE_USB:
			extraParams.putInt(ConnectionType.EXTRA_USB_BAUD_RATE, dpPrefs.getUsbBaudRate());
			connParams = new ConnectionParameter(connectionType, extraParams);
			break;

		case ConnectionType.TYPE_UDP:
			extraParams.putInt(ConnectionType.EXTRA_UDP_SERVER_PORT, dpPrefs.getUdpServerPort());
			connParams = new ConnectionParameter(connectionType, extraParams);
			break;

		case ConnectionType.TYPE_TCP:
			extraParams.putString(ConnectionType.EXTRA_TCP_SERVER_IP, dpPrefs.getTcpServerIp());
			extraParams.putInt(ConnectionType.EXTRA_TCP_SERVER_PORT, dpPrefs.getTcpServerPort());
			connParams = new ConnectionParameter(connectionType, extraParams);
			break;

		case ConnectionType.TYPE_BLUETOOTH:
			String btAddress = dpPrefs.getBluetoothDeviceAddress();
			if (TextUtils.isEmpty(btAddress)) {
				connParams = null;
				startActivity(new Intent(getApplicationContext(), BluetoothDevicesActivity.class)
						.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

			} else {
				extraParams.putString(ConnectionType.EXTRA_BLUETOOTH_ADDRESS, btAddress);
				connParams = new ConnectionParameter(connectionType, extraParams);
			}
			break;

		default:
			Log.e(TAG, "Unrecognized connection type: " + connectionType);
			connParams = null;
			break;
		}

		return connParams;
	}
}
