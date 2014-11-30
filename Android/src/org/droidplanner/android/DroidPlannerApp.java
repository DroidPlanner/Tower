package org.droidplanner.android;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.o3dr.android.client.Drone;
import com.o3dr.android.client.ServiceManager;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.ServiceListener;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;
import com.o3dr.services.android.lib.drone.connection.DroneSharePrefs;
import com.o3dr.services.android.lib.drone.connection.StreamRates;
import com.o3dr.services.android.lib.drone.event.Event;

import org.droidplanner.android.activities.helpers.BluetoothDevicesActivity;
import org.droidplanner.android.notifications.NotificationHandler;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.utils.analytics.GAUtils;
import org.droidplanner.android.utils.file.IO.ExceptionWriter;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

import java.util.ArrayList;
import java.util.List;

public class DroidPlannerApp extends Application implements DroneListener, ServiceListener {

	private static final long DELAY_TO_DISCONNECTION = 30000l; // ms

	private static final String CLAZZ_NAME = DroidPlannerApp.class.getName();
	private static final String TAG = DroidPlannerApp.class.getSimpleName();

	public static final String ACTION_TOGGLE_DRONE_CONNECTION = CLAZZ_NAME
			+ ".ACTION_TOGGLE_DRONE_CONNECTION";
	public static final String EXTRA_ESTABLISH_CONNECTION = "extra_establish_connection";

    public static final String ACTION_DRONE_CONNECTION_FAILED = CLAZZ_NAME
            + ".ACTION_DRONE_CONNECTION_FAILED";

    public static final String EXTRA_CONNECTION_FAILED_ERROR_CODE = "extra_connection_failed_error_code";

    public static final String EXTRA_CONNECTION_FAILED_ERROR_MESSAGE = "extra_connection_failed_error_message";

    public static final String ACTION_DRONE_EVENT = CLAZZ_NAME + ".ACTION_DRONE_EVENT";
    public static final String EXTRA_DRONE_EVENT = "extra_drone_event";

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (ACTION_TOGGLE_DRONE_CONNECTION.equals(action)) {
				boolean connect = intent.getBooleanExtra(EXTRA_ESTABLISH_CONNECTION,
						!drone.isConnected());

				if (connect)
					connectToDrone();
				else
					disconnectFromDrone();
			}
		}
	};

    @Override
    public void onServiceConnected() {
        if(notificationHandler == null) {
            notificationHandler = new NotificationHandler(getApplicationContext(), drone);
        }

        if(!drone.isStarted()) {
            this.drone.start();
            this.drone.registerDroneListener(this);
        }

        notifyApiConnected();
    }

    @Override
    public void onServiceInterrupted() {
        notifyApiDisconnected();
    }

    public interface ApiListener {
		void onApiConnected();

		void onApiDisconnected();
	}

	private final Runnable disconnectionTask = new Runnable() {
		@Override
		public void run() {
            drone.destroy();
            serviceMgr.disconnect();

            if(notificationHandler != null) {
                notificationHandler.terminate();
                notificationHandler = null;
            }
            
            handler.removeCallbacks(this);
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

    private ServiceManager serviceMgr;
    private Drone drone;

    private MissionProxy missionProxy;
    private DroidPlannerPrefs dpPrefs;
    private LocalBroadcastManager lbm;
	private NotificationHandler notificationHandler;

	@Override
	public void onCreate() {
		super.onCreate();
		final Context context = getApplicationContext();

        dpPrefs = new DroidPlannerPrefs(context);
        lbm = LocalBroadcastManager.getInstance(context);

        serviceMgr = new ServiceManager(context);
        drone = new Drone(serviceMgr, handler);
        missionProxy = new MissionProxy(context, this.drone);

		exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(dpExceptionHandler);

		GAUtils.initGATracker(this);
		GAUtils.startNewSession(context);

		registerReceiver(broadcastReceiver, new IntentFilter(ACTION_TOGGLE_DRONE_CONNECTION));
	}

	public void addApiListener(ApiListener listener) {
		if (listener == null)
			return;

        handler.removeCallbacks(disconnectionTask);
        boolean isServiceConnected = serviceMgr.isServiceConnected();
		if (isServiceConnected)
			listener.onApiConnected();

        if(apiListeners.isEmpty() && !isServiceConnected) {
            serviceMgr.connect(this);
        }

        apiListeners.add(listener);
	}

	public void removeApiListener(ApiListener listener) {
		if (listener != null) {
			apiListeners.remove(listener);
			listener.onApiDisconnected();
		}

		shouldWeTerminate();
	}

	private void shouldWeTerminate() {
		if (apiListeners.isEmpty() && !drone.isConnected()) {
			// Wait 30s, then disconnect the service binding.
			handler.postDelayed(disconnectionTask, DELAY_TO_DISCONNECTION);
		}
	}

	private void notifyApiConnected() {
		if (apiListeners.isEmpty())
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

    public void connectToDrone(){
        final ConnectionParameter connParams = retrieveConnectionParameters();
        if(connParams == null)
            return;

        boolean isDroneConnected = drone.isConnected();
        if(!connParams.equals(drone.getConnectionParameter()) && isDroneConnected) {
            drone.disconnect();
            isDroneConnected = false;
        }

        if(!isDroneConnected)
            drone.connect(connParams);
    }

    public static void connectToDrone(Context context){
        context.sendBroadcast(new Intent(DroidPlannerApp.ACTION_TOGGLE_DRONE_CONNECTION)
                .putExtra(DroidPlannerApp.EXTRA_ESTABLISH_CONNECTION, true));
    }

    public static void disconnectFromDrone(Context context){
        context.sendBroadcast(new Intent(DroidPlannerApp.ACTION_TOGGLE_DRONE_CONNECTION)
                .putExtra(DroidPlannerApp.EXTRA_ESTABLISH_CONNECTION, false));
    }

    public void disconnectFromDrone(){
        if(drone.isConnected())
            drone.disconnect();
    }

    public Drone getDrone(){
        return this.drone;
    }

    public MissionProxy getMissionProxy(){
        return this.missionProxy;
    }

    private ConnectionParameter retrieveConnectionParameters() {
        final int connectionType = dpPrefs.getConnectionParameterType();
        final StreamRates rates = dpPrefs.getStreamRates();
        Bundle extraParams = new Bundle();
        final DroneSharePrefs droneSharePrefs = new DroneSharePrefs(dpPrefs.getDroneshareLogin(),
                dpPrefs.getDronesharePassword(), dpPrefs.getDroneshareEnabled(),
                dpPrefs.getLiveUploadEnabled());

        ConnectionParameter connParams;
        switch (connectionType) {
            case ConnectionType.TYPE_USB:
                extraParams.putInt(ConnectionType.EXTRA_USB_BAUD_RATE, dpPrefs.getUsbBaudRate());
                connParams = new ConnectionParameter(connectionType, extraParams, rates,
                        droneSharePrefs);
                break;

            case ConnectionType.TYPE_UDP:
                extraParams.putInt(ConnectionType.EXTRA_UDP_SERVER_PORT, dpPrefs.getUdpServerPort());
                connParams = new ConnectionParameter(connectionType, extraParams, rates,
                        droneSharePrefs);
                break;

            case ConnectionType.TYPE_TCP:
                extraParams.putString(ConnectionType.EXTRA_TCP_SERVER_IP, dpPrefs.getTcpServerIp());
                extraParams.putInt(ConnectionType.EXTRA_TCP_SERVER_PORT, dpPrefs.getTcpServerPort());
                connParams = new ConnectionParameter(connectionType, extraParams, rates,
                        droneSharePrefs);
                break;

            case ConnectionType.TYPE_BLUETOOTH:
                String btAddress = dpPrefs.getBluetoothDeviceAddress();
                if (TextUtils.isEmpty(btAddress)) {
                    connParams = null;
                    startActivity(new Intent(getApplicationContext(),
                            BluetoothDevicesActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

                } else {
                    extraParams.putString(ConnectionType.EXTRA_BLUETOOTH_ADDRESS, btAddress);
                    connParams = new ConnectionParameter(connectionType, extraParams, rates,
                            droneSharePrefs);
                }
                break;

            default:
                Log.e(TAG, "Unrecognized connection type: " + connectionType);
                connParams = null;
                break;
        }

        return connParams;
    }

    @Override
    public void onDroneConnectionFailed(ConnectionResult result){
        String errorMsg = result.getErrorMessage();
        Toast.makeText(getApplicationContext(), "Connection failed: " + errorMsg,
                Toast.LENGTH_LONG).show();

        lbm.sendBroadcast(new Intent(ACTION_DRONE_CONNECTION_FAILED)
                .putExtra(EXTRA_CONNECTION_FAILED_ERROR_CODE, result.getErrorCode())
                .putExtra(EXTRA_CONNECTION_FAILED_ERROR_MESSAGE, result.getErrorMessage()));
    }

    @Override
    public void onDroneEvent(String event, Bundle extras){
        if (Event.EVENT_CONNECTED.equals(event)) {
            handler.removeCallbacks(disconnectionTask);
            if(notificationHandler == null) {
                notificationHandler = new NotificationHandler(getApplicationContext(), drone);
            }
        }
        else if (Event.EVENT_DISCONNECTED.equals(event)) {
            shouldWeTerminate();
        }

        lbm.sendBroadcast(new Intent(ACTION_DRONE_EVENT).putExtra(EXTRA_DRONE_EVENT, event));

        final Intent droneIntent = new Intent(event);
        if(extras != null)
            droneIntent.putExtras(extras);
        lbm.sendBroadcast(droneIntent);
    }

    @Override
    public void onDroneServiceInterrupted(String errorMsg){
        if(errorMsg != null)
            Log.e(TAG, errorMsg);
    }
}
