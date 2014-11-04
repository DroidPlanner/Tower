package org.droidplanner.android;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.ox3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.ox3dr.services.android.lib.drone.connection.ConnectionType;
import com.ox3dr.services.android.lib.model.IDroidPlannerApi;
import com.ox3dr.services.android.lib.model.IDroidPlannerServices;

import org.droidplanner.android.activities.helpers.BluetoothDevicesActivity;
import org.droidplanner.android.api.DPApiCallback;
import org.droidplanner.android.api.DroneApi;
import org.droidplanner.android.communication.service.UploaderService;
import org.droidplanner.android.utils.analytics.GAUtils;
import org.droidplanner.android.utils.file.IO.ExceptionWriter;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DroidPlannerApp extends Application {

	private static final long DELAY_TO_DISCONNECTION = 60000l; // ms

	private static final String CLAZZ_NAME = DroidPlannerApp.class.getName();
	private static final String TAG = DroidPlannerApp.class.getSimpleName();

	public static final String ACTION_TOGGLE_DRONE_CONNECTION = CLAZZ_NAME
			+ ".ACTION_TOGGLE_DRONE_CONNECTION";
	public static final String EXTRA_ESTABLISH_CONNECTION = "extra_establish_connection";

	private static final int API_UNBOUND = 0;
	private static final int API_BOUND = 1;

	public interface ApiListener {
		void onApiConnected(DroneApi api);

		void onApiDisconnected();
	}

	private final static IntentFilter intentFilter = new IntentFilter(
			ACTION_TOGGLE_DRONE_CONNECTION);

	private final AtomicInteger apiBindingState = new AtomicInteger(API_UNBOUND);
	private final DPApiCallback dpCallback = new DPApiCallback(this);

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (ACTION_TOGGLE_DRONE_CONNECTION.equals(action)) {
				if (ox3drServices != null) {
					boolean connectionState = intent.getBooleanExtra(EXTRA_ESTABLISH_CONNECTION,
							!isDpApiConnected());
					if (connectionState)
						connectToDrone();
					else
						disconnectFromDrone();
				}
			}
		}
	};

	private final ServiceConnection ox3drServicesConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			ox3drServices = IDroidPlannerServices.Stub.asInterface(service);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			notifyApiDisconnected();
			dpApi = null;
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

    private final DroneApi droneApi = new DroneApi(null);

	private IDroidPlannerServices ox3drServices;
	private IDroidPlannerApi dpApi;

	private DroidPlannerPrefs dpPrefs;

	@Override
	public void onCreate() {
		super.onCreate();
		final Context context = getApplicationContext();

		dpPrefs = new DroidPlannerPrefs(context);

		exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(dpExceptionHandler);

		final LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
		lbm.registerReceiver(broadcastReceiver, intentFilter);

		GAUtils.initGATracker(this);
		GAUtils.startNewSession(context);

		// Any time the application is started, do a quick scan to see if we
		// need any uploads
		startService(UploaderService.createIntent(this));
	}

	public DroneApi getDroneApi(){
        return droneApi;
    }

	public void addApiListener(ApiListener listener) {
		if (listener == null)
			return;

		if (dpApi != null)
				listener.onApiConnected(droneApi);

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
		if (apiListeners.isEmpty() || !isDpApiConnected())
			return;

			for (ApiListener listener : apiListeners)
				listener.onApiConnected(droneApi);
	}

	private void notifyApiDisconnected() {
		if (apiListeners.isEmpty())
			return;

		for (ApiListener listener : apiListeners)
			listener.onApiDisconnected();
	}

	private void connectToDrone() {
		if (ox3drServices == null || isDpApiConnected())
			return;

		// Retrieve the connection parameters.
		final ConnectionParameter connParams = retrieveConnectionParameters();
		if (connParams == null) {
			Log.e(TAG, "Invalid connection parameters");
			return;
		}

		try {
			dpApi = ox3drServices.connectToDrone(connParams, dpCallback);
            droneApi.setDpApi(dpApi);
			notifyApiConnected();
		} catch (RemoteException e) {
			Log.e(TAG, "Unable to retrieve a droidplanner api connection.", e);
		}
	}

	private void disconnectFromDrone() {
		if (!isDpApiConnected())
			return; // Nothing to do. It's already disconnected.

		try {
			dpApi.disconnectFromDrone();
		} catch (RemoteException e) {
			Log.e(TAG, "Error while disconnecting from the droidplanner api", e);
		}

		notifyApiDisconnected();
        droneApi.setDpApi(null);
		dpApi = null;
	}

	public boolean isDpApiConnected() {
		return dpApi != null;
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
