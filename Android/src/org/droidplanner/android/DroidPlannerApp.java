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

import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.TowerListener;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;
import com.o3dr.services.android.lib.drone.connection.DroneSharePrefs;

import org.droidplanner.android.activities.helpers.BluetoothDevicesActivity;
import org.droidplanner.android.maps.providers.google_map.tiles.mapbox.offline.MapDownloader;
import org.droidplanner.android.notifications.NotificationHandler;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.utils.Utils;
import org.droidplanner.android.utils.analytics.GAUtils;
import org.droidplanner.android.utils.file.IO.ExceptionWriter;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class DroidPlannerApp extends Application implements DroneListener, TowerListener {

    private static final long DELAY_TO_DISCONNECTION = 1000l; // ms

    private static final String TAG = DroidPlannerApp.class.getSimpleName();

    public static final String ACTION_TOGGLE_DRONE_CONNECTION = Utils.PACKAGE_NAME
            + ".ACTION_TOGGLE_DRONE_CONNECTION";
    public static final String EXTRA_ESTABLISH_CONNECTION = "extra_establish_connection";

    public static final String ACTION_DRONE_CONNECTION_FAILED = Utils.PACKAGE_NAME
            + ".ACTION_DRONE_CONNECTION_FAILED";

    public static final String EXTRA_CONNECTION_FAILED_ERROR_CODE = "extra_connection_failed_error_code";

    public static final String EXTRA_CONNECTION_FAILED_ERROR_MESSAGE = "extra_connection_failed_error_message";

    public static final String ACTION_DRONE_EVENT = Utils.PACKAGE_NAME + ".ACTION_DRONE_EVENT";
    public static final String EXTRA_DRONE_EVENT = "extra_drone_event";

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case ACTION_TOGGLE_DRONE_CONNECTION:
                    boolean connect = intent.getBooleanExtra(EXTRA_ESTABLISH_CONNECTION,
                            !drone.isConnected());

                    if (connect)
                        connectToDrone();
                    else
                        disconnectFromDrone();
                    break;
            }
        }
    };

    @Override
    public void onTowerConnected() {
        if (notificationHandler == null) {
            notificationHandler = new NotificationHandler(getApplicationContext(), drone);
        }

        drone.unregisterDroneListener(this);
        controlTower.registerDrone(drone, handler);
        drone.registerDroneListener(this);

        notifyApiConnected();
    }

    @Override
    public void onTowerDisconnected() {
        notifyApiDisconnected();
    }

    public DroidPlannerPrefs getAppPreferences() {
        return dpPrefs;
    }

    public interface ApiListener {
        void onApiConnected();

        void onApiDisconnected();
    }

    private final Runnable disconnectionTask = new Runnable() {
        @Override
        public void run() {
            controlTower.unregisterDrone(drone);
            controlTower.disconnect();

            if (notificationHandler != null) {
                notificationHandler.terminate();
                notificationHandler = null;
            }

            handler.removeCallbacks(this);
        }
    };

    private final Handler handler = new Handler();
    private final List<ApiListener> apiListeners = new ArrayList<ApiListener>();

    private Thread.UncaughtExceptionHandler exceptionHandler;

    private ControlTower controlTower;
    private Drone drone;

    private MissionProxy missionProxy;
    private DroidPlannerPrefs dpPrefs;
    private LocalBroadcastManager lbm;
    private NotificationHandler notificationHandler;
    private MapDownloader mapDownloader;

    @Override
    public void onCreate() {
        super.onCreate();
        final Context context = getApplicationContext();

        dpPrefs = new DroidPlannerPrefs(context);
        lbm = LocalBroadcastManager.getInstance(context);
        mapDownloader = new MapDownloader(context);

        controlTower = new ControlTower(context);
        drone = new Drone(context);
        missionProxy = new MissionProxy(context, this.drone);

        final Thread.UncaughtExceptionHandler dpExceptionHandler = new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                new ExceptionWriter(ex).saveStackTraceToSD(context);
                exceptionHandler.uncaughtException(thread, ex);
            }
        };

        exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(dpExceptionHandler);

        GAUtils.initGATracker(this);
        GAUtils.startNewSession(context);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_TOGGLE_DRONE_CONNECTION);

        registerReceiver(broadcastReceiver, intentFilter);
    }

    public MapDownloader getMapDownloader() {
        return mapDownloader;
    }

    public void addApiListener(ApiListener listener) {
        if (listener == null)
            return;

        handler.removeCallbacks(disconnectionTask);
        boolean isTowerConnected = controlTower.isTowerConnected();
        if (isTowerConnected)
            listener.onApiConnected();

        if (!isTowerConnected) {
            try {
                controlTower.connect(this);
            } catch (IllegalStateException e) {
                //Ignore
            }
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

    public void connectToDrone() {
        final ConnectionParameter connParams = retrieveConnectionParameters();
        if (connParams == null)
            return;

        boolean isDroneConnected = drone.isConnected();
        if (!connParams.equals(drone.getConnectionParameter()) && isDroneConnected) {
            drone.disconnect();
            isDroneConnected = false;
        }

        if (!isDroneConnected)
            drone.connect(connParams);
    }

    public static void connectToDrone(Context context) {
        context.sendBroadcast(new Intent(DroidPlannerApp.ACTION_TOGGLE_DRONE_CONNECTION)
                .putExtra(DroidPlannerApp.EXTRA_ESTABLISH_CONNECTION, true));
    }

    public static void disconnectFromDrone(Context context) {
        context.sendBroadcast(new Intent(DroidPlannerApp.ACTION_TOGGLE_DRONE_CONNECTION)
                .putExtra(DroidPlannerApp.EXTRA_ESTABLISH_CONNECTION, false));
    }

    public void disconnectFromDrone() {
        if (drone.isConnected())
            drone.disconnect();
    }

    public Drone getDrone() {
        return this.drone;
    }

    public MissionProxy getMissionProxy() {
        return this.missionProxy;
    }

    private ConnectionParameter retrieveConnectionParameters() {
        final int connectionType = dpPrefs.getConnectionParameterType();
        Bundle extraParams = new Bundle();
        final DroneSharePrefs droneSharePrefs = new DroneSharePrefs(dpPrefs.getDroneshareLogin(),
                dpPrefs.getDronesharePassword(), dpPrefs.isDroneshareEnabled(),
                dpPrefs.isLiveUploadEnabled());

        ConnectionParameter connParams;
        switch (connectionType) {
            case ConnectionType.TYPE_USB:
                extraParams.putInt(ConnectionType.EXTRA_USB_BAUD_RATE, dpPrefs.getUsbBaudRate());
                connParams = new ConnectionParameter(connectionType, extraParams, droneSharePrefs);
                break;

            case ConnectionType.TYPE_UDP:
                extraParams.putInt(ConnectionType.EXTRA_UDP_SERVER_PORT, dpPrefs.getUdpServerPort());
                if(dpPrefs.isUdpPingEnabled()){
                    extraParams.putString(ConnectionType.EXTRA_UDP_PING_RECEIVER_IP, dpPrefs.getUdpPingReceiverIp());
                    extraParams.putInt(ConnectionType.EXTRA_UDP_PING_RECEIVER_PORT, dpPrefs.getUdpPingReceiverPort());
                    extraParams.putByteArray(ConnectionType.EXTRA_UDP_PING_PAYLOAD, "Hello".getBytes());
                }
                connParams = new ConnectionParameter(connectionType, extraParams, droneSharePrefs);
                break;

            case ConnectionType.TYPE_TCP:
                extraParams.putString(ConnectionType.EXTRA_TCP_SERVER_IP, dpPrefs.getTcpServerIp());
                extraParams.putInt(ConnectionType.EXTRA_TCP_SERVER_PORT, dpPrefs.getTcpServerPort());
                connParams = new ConnectionParameter(connectionType, extraParams, droneSharePrefs);
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
                    connParams = new ConnectionParameter(connectionType, extraParams, droneSharePrefs);
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
    public void onDroneConnectionFailed(ConnectionResult result) {
        String errorMsg = result.getErrorMessage();
        Toast.makeText(getApplicationContext(), "Connection failed: " + errorMsg,
                Toast.LENGTH_LONG).show();

        lbm.sendBroadcast(new Intent(ACTION_DRONE_CONNECTION_FAILED)
                .putExtra(EXTRA_CONNECTION_FAILED_ERROR_CODE, result.getErrorCode())
                .putExtra(EXTRA_CONNECTION_FAILED_ERROR_MESSAGE, result.getErrorMessage()));
    }

    @Override
    public void onDroneEvent(String event, Bundle extras) {
        switch (event) {
            case AttributeEvent.STATE_CONNECTED:
                handler.removeCallbacks(disconnectionTask);
                if (notificationHandler == null) {
                    notificationHandler = new NotificationHandler(getApplicationContext(), drone);
                }
                break;
            case AttributeEvent.STATE_DISCONNECTED:
                shouldWeTerminate();
                break;
        }

        lbm.sendBroadcast(new Intent(ACTION_DRONE_EVENT).putExtra(EXTRA_DRONE_EVENT, event));

        final Intent droneIntent = new Intent(event);
        if (extras != null)
            droneIntent.putExtras(extras);
        lbm.sendBroadcast(droneIntent);
    }

    @Override
    public void onDroneServiceInterrupted(String errorMsg) {
        controlTower.unregisterDrone(drone);
        if (notificationHandler != null) {
            notificationHandler.terminate();
            notificationHandler = null;
        }

        if (!TextUtils.isEmpty(errorMsg))
            Log.e(TAG, errorMsg);
    }
}
