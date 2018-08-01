package co.aerobotics.android;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.text.method.BaseKeyListener;
import android.util.Log;
import android.widget.Toast;

import co.aerobotics.android.BuildConfig;

import co.aerobotics.android.data.DJIFlightControllerState;
import co.aerobotics.android.data.VolleyRequest;
import co.aerobotics.android.droneshare.data.DroneShareDB;
import co.aerobotics.android.data.AeroviewPolygons;
import co.aerobotics.android.graphic.map.CameraMarker;
import co.aerobotics.android.graphic.map.PolygonData;
import co.aerobotics.android.utils.file.IO.ExceptionWriter;
import co.aerobotics.android.utils.prefs.DroidPlannerPrefs;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.ndk.CrashlyticsNdk;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.LinkListener;
import com.o3dr.android.client.interfaces.TowerListener;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;
import com.o3dr.services.android.lib.gcs.link.LinkConnectionStatus;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.secneo.sdk.Helper;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;


import co.aerobotics.android.activities.helpers.BluetoothDevicesActivity;
import co.aerobotics.android.droneshare.UploaderService;
import co.aerobotics.android.droneshare.data.SessionDB;
import co.aerobotics.android.proxy.mission.MissionProxy;
import co.aerobotics.android.utils.LogToFileTree;
import co.aerobotics.android.utils.TLogUtils;
import co.aerobotics.android.utils.Utils;
import co.aerobotics.android.utils.sound.SoundManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

//import dji.common.camera.SDCardState;
import dji.common.camera.SSDState;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.flightcontroller.FlightAssistant;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.MissionControl;
import dji.sdk.products.Aircraft;
import dji.sdk.products.HandHeld;
import dji.sdk.sdkmanager.DJISDKManager;
import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class DroidPlannerApp extends MultiDexApplication implements DroneListener, TowerListener, LinkListener {
    public static String MIXPANEL_TOKEN = "";
    // DJI Application variables
    public static final String FLAG_CONNECTION_CHANGE = "dji_sdk_connection_change";
    private static BaseProduct product;
    private static FlightController flightController;
    private static String firmwareVersion;
    private static Boolean isNewFirmwareVersion;
    public boolean isSDKRegistered = false;

    // DJI variables end
    private static DroidPlannerApp instance;

    private static final long DELAY_TO_DISCONNECTION = 1000L; // ms

    private static final String TAG = "DroidPlannerApp";

    public static final String ACTION_TOGGLE_DRONE_CONNECTION = Utils.PACKAGE_NAME
            + ".ACTION_TOGGLE_DRONE_CONNECTION";
    public static final String EXTRA_ESTABLISH_CONNECTION = "extra_establish_connection";

    public static final String REGISTER_DJI_SDK = "register_dji_sdk";


    private static final long EVENTS_DISPATCHING_PERIOD = 200L; //MS
    private static final long INVALID_SESSION_ID = -1L;

    private static final AtomicBoolean isCellularNetworkOn = new AtomicBoolean(false);

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

    public boolean isSDCardInserted = false;
    private Application appInstance;

    public void setContext(Application application) {
        appInstance = application;
    }

    @Override
    public Context getApplicationContext() {
        return appInstance;
    }

    @Override
    public void onTowerConnected() {
        Timber.d("Connecting to the control tower.");
        drone.unregisterDroneListener(this);

        controlTower.registerDrone(drone, handler);
        drone.registerDroneListener(this);

        notifyApiConnected();
    }

    @Override
    public void onTowerDisconnected() {
        Timber.d("Disconnection from the control tower.");
        notifyApiDisconnected();
    }

    @Override
    public void onLinkStateUpdated(@NonNull LinkConnectionStatus connectionStatus) {
        switch(connectionStatus.getStatusCode()){
            case LinkConnectionStatus.FAILED:
                Bundle extras = connectionStatus.getExtras();
                String errorMsg = null;
                if (extras != null) {
                    errorMsg = extras.getString(LinkConnectionStatus.EXTRA_ERROR_MSG);
                }

                Toast.makeText(getApplicationContext(), "Connection failed: " + errorMsg,
                        Toast.LENGTH_LONG).show();
                break;
        }
    }

    public interface ApiListener {
        void onApiConnected();

        void onApiDisconnected();
    }

    private final Runnable disconnectionTask = new Runnable() {
        @Override
        public void run() {
            Timber.d("Starting control tower disconnect process...");
            controlTower.unregisterDrone(drone);
            controlTower.disconnect();

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

    private LogToFileTree logToFileTree;
    private SoundManager soundManager;

    private long currentSessionId = INVALID_SESSION_ID;
    private SessionDB sessionDB;
    private DroneShareDB droneShareDb;
    private MixpanelAPI mixpanelInstance;

    public List<String> selectedPolygons = new ArrayList<>();

    public static DroidPlannerApp getInstance() {
        return instance;
    }

    public static synchronized BaseProduct getProductInstance() {
        if (null == product) {
            product = DJISDKManager.getInstance().getProduct();
        }
        return product;
    }

    public static synchronized FlightController getFlightController() {
        if (flightController == null) {
            if (getProductInstance() instanceof Aircraft) {
                flightController = ((Aircraft) getProductInstance()).getFlightController();
            }
        }
        return flightController;
    }

    public void getFirmwareVersion() {
        if (getFlightController() != null) {
            getFlightController().getFirmwareVersion(new CommonCallbacks.CompletionCallbackWith<String>() {
                @Override
                public void onSuccess(String s) {
                    firmwareVersion = s;
                    String[] strArray = firmwareVersion.split("\\.");
                    int[] intArray = new int[strArray.length];
                    for (int i = 0; i < strArray.length; i++) {
                        intArray[i] = Integer.parseInt(strArray[i]);
                    }
                    isNewFirmwareVersion = intArray[0] >= 3 && intArray[1] >= 2 && intArray[2] >= 10;
                }

                @Override
                public void onFailure(DJIError djiError) {

                }
            });
        }
    }

    public static synchronized Boolean isFirmwareNewVersion() {
        /**
         * Check for flight controller version 3.2.10
         */
        return isNewFirmwareVersion;
    }

    public static synchronized MissionControl getMissionControlInstance(){
        if (DJISDKManager.getInstance().getMissionControl() == null) return null;
        return (DJISDKManager.getInstance().getMissionControl());
    }

    public static boolean isProductConnected(){
        return getProductInstance() != null && getProductInstance().isConnected();
    }

    public static boolean isAircraftConnected() {
        return getProductInstance() != null && getProductInstance() instanceof Aircraft;
    }


    public static synchronized Aircraft getAircraftInstance() {
        if (!isAircraftConnected()) return null;
        return (Aircraft) getProductInstance();
    }


    public static synchronized Camera getCameraInstance() {

        if (getProductInstance() == null) return null;

        Camera camera = null;

        if (getProductInstance() instanceof Aircraft){
            camera = ((Aircraft) getProductInstance()).getCamera();

        } else if (getProductInstance() instanceof HandHeld) {
            camera = ((HandHeld) getProductInstance()).getCamera();
        }

        return camera;
    }

    public static RefWatcher getRefWatcher(Context context) {
        DroidPlannerApp application = (DroidPlannerApp) context.getApplicationContext();
        return application.refWatcher;
    }

    public synchronized void addPolygon(String id){
        selectedPolygons.add(id);
    }

    public synchronized void removePolygon(String id){
        selectedPolygons.remove(id);
    }

    public synchronized List<String> getSelectedPolygons(){
        return selectedPolygons;
    }

    public Map<String,PolygonData> polygonMap = new HashMap<>();

    public List<LatLng> cameraPositions = new ArrayList<>();

    //Maps Camera marker LatLong to a rotation
    public Map<LatLng, Float> cameraMarkerInfoMap = new ConcurrentHashMap<>();
    public List<CameraMarker> cameraMarkers = new CopyOnWriteArrayList<>();

    public Boolean hideTelemetry = false;

    private RefWatcher refWatcher;

    public Handler mHandler;
    //private BaseProduct.BaseProductListener mDJIBaseProductListener;

    private BaseComponent.ComponentListener mDJIComponentListener;
    private static BaseProduct mProduct;
    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        Helper.install(DroidPlannerApp.this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final Context context = getApplicationContext();
        Fabric.with(context, new Crashlytics());
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(co.aerobotics.android.R.string.com_dji_android_PREF_FILE_KEY),Context.MODE_PRIVATE);
        Crashlytics.setUserIdentifier(sharedPreferences.getString(context.getString(co.aerobotics.android.R.string.username), ""));
        dpPrefs = DroidPlannerPrefs.getInstance(context);
        lbm = LocalBroadcastManager.getInstance(context);
        soundManager = new SoundManager(context);
        initMixpanelAnalytics();
        VolleyRequest.getInstance(context);
        //initLoggingAndAnalytics();
        initDronekit();
        initDatabases();
        mHandler = new Handler(Looper.getMainLooper());
        instance = this;
        DJISDKManager.getInstance().getProduct();
        AeroviewPolygons aeroviewPolygons = new AeroviewPolygons(context);
        if(isNetworkAvailable()) {
            aeroviewPolygons.executeGetFarmsTask();
            aeroviewPolygons.executeGetFarmOrchardsTask();
        } else {
            aeroviewPolygons.addPolygonsToMap();
        }

//        mDJIBaseProductListener = new BaseProduct.BaseProductListener() {
//            @Override
//            public void onComponentChange(BaseProduct.ComponentKey key, BaseComponent oldComponent, BaseComponent newComponent) {
//                if(newComponent != null) {
//                    newComponent.setComponentListener(mDJIComponentListener);
//                    getFirmwareVersion();
//                }
//                notifyStatusChange();
//            }
//            @Override
//            public void onConnectivityChange(boolean isConnected) {
//                mixpanelInstance.track("FPA: ConnectedToDrone");
//                notifyStatusChange();
//            }
//        };

        mDJIComponentListener = new BaseComponent.ComponentListener() {
            @Override
            public void onConnectivityChange(boolean isConnected) {
                if (isConnected) {
                    getFirmwareVersion();
                }
                notifyStatusChange();
            }
        };
    }

    public String getMixpanelToken() {
        String token = "";
        try {
            token = Utils.getProperty("mixpanelToken", getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return token;
    }

    public boolean isNetworkAvailable() {
        final Context context = getApplicationContext();
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showToast(final String toastMsg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
            }
        });
    }


    /**
     * When starting SDK services, an instance of interface DJISDKManager.DJISDKManagerCallback will be used to listen to
     * the SDK Registration result and the product changing.
     */
    DJISDKManager.SDKManagerCallback mDJISDKManagerCallback = new DJISDKManager.SDKManagerCallback() {
        //Listens to the SDK registration result
        @Override
        public void onRegister(DJIError error) {
            if(error == DJISDKError.REGISTRATION_SUCCESS) {
                DJISDKManager.getInstance().startConnectionToProduct();
                // DJISDKManager.getInstance().enableBridgeModeWithBridgeAppIP("192.168.100.165");
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mixpanelInstance.track("FPA: DjiSdkRegisterSuccess");
                    }
                });
            } else {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mixpanelInstance.track("FPA: DjiSdkRegisterFailed");
                        Toast.makeText(getApplicationContext(), "Register Failed, check network is available", Toast.LENGTH_LONG).show();
                    }
                });
            }
            Log.e("TAG", error.toString());
        }
        //Listens to the connected product changing, including two parts, component changing or product connection changing.
//        @Override
//        public void onProductChange(BaseProduct oldProduct, BaseProduct newProduct) {
//            mProduct = newProduct;
//            if(mProduct != null) {
//                mixpanelInstance.track("FPA: ConnectedToDrone");
//                mProduct.setBaseProductListener(mDJIBaseProductListener);
//                getFirmwareVersion();
//            }
//            notifyStatusChange();
//        }

        @Override
        public void onProductDisconnect() {

        }

        @Override
        public void onProductConnect(BaseProduct baseProduct) {
            mProduct = baseProduct;
            if(mProduct!=null){
                mixpanelInstance.track("FPA: ConnectedToDrone");
                //add listener to the next product?
                getFirmwareVersion();
            }
            notifyStatusChange();
        }

        @Override
        public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent baseComponent, BaseComponent baseComponent1) {
            if(baseComponent1 != null) {
                baseComponent1.setComponentListener(mDJIComponentListener);
                getFirmwareVersion();
            }
            notifyStatusChange();
        }
    };

    public void registerSDK(){
        if (!DJISDKManager.getInstance().hasSDKRegistered()) {
            DJISDKManager.getInstance().registerApp(getApplicationContext(), mDJISDKManagerCallback);
        }
/*
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                DJISDKManager.getInstance().registerApp(getApplicationContext(), mDJISDKManagerCallback);
            }
        }); */
    }


    /*
    Functions from DJI Application
     */

    private void notifyStatusChange() {
        mHandler.removeCallbacks(updateRunnable);
        mHandler.postDelayed(updateRunnable, 500);
    }

    private Runnable updateRunnable = new Runnable() {

        @Override
        public void run() {
            Intent intent = new Intent(FLAG_CONNECTION_CHANGE);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    };

    private void initMixpanelAnalytics(){
        mixpanelInstance = MixpanelAPI.getInstance(this, getMixpanelToken());
    }

    private void initLoggingAndAnalytics(){
        //Init leak canary
        LeakCanary.install(this);

        final Context context = getApplicationContext();

        final Thread.UncaughtExceptionHandler dpExceptionHandler = new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                new ExceptionWriter(ex).saveStackTraceToSD(context);
                exceptionHandler.uncaughtException(thread, ex);
            }
        };

        exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(dpExceptionHandler);

        if (BuildConfig.WRITE_LOG_FILE) {
            logToFileTree = new LogToFileTree();
            Timber.plant(logToFileTree);
        } else if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        if(BuildConfig.ENABLE_CRASHLYTICS) {
            Fabric.with(context, new Crashlytics(), new CrashlyticsNdk());
        }
    }

    private void initDronekit(){
        Context context = getApplicationContext();

        controlTower = new ControlTower(context);
        drone = new Drone(context);
        missionProxy = new MissionProxy(this, this.drone);
/*
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(REGISTER_DJI_SDK);
        registerReceiver(broadcastReceiver, intentFilter);
*/
    }

    private void initDatabases(){
        Context context = getApplicationContext();
        sessionDB = new SessionDB(context);
        droneShareDb = new DroneShareDB(context);
        cleanupDroneSessions();
    }

    public void addApiListener(ApiListener listener) {
        if (listener == null)
            return;

        handler.removeCallbacks(disconnectionTask);
        boolean isTowerConnected = controlTower.isTowerConnected();
        if (isTowerConnected) {
            listener.onApiConnected();
        }

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
            if (controlTower.isTowerConnected())
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
            Timber.d("Drone disconnection before reconnect attempt with different parameters.");
            drone.disconnect();
            isDroneConnected = false;
        }

        if (!isDroneConnected) {
            Timber.d("Connecting to drone using parameter %s", connParams);
            drone.connect(connParams, this);
        }
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
        if (drone.isConnected()) {
            Timber.d("Disconnecting from drone.");
            drone.disconnect();
        }
    }

    public Drone getDrone() {
        return this.drone;
    }

    public MissionProxy getMissionProxy() {
        return this.missionProxy;
    }

    private ConnectionParameter retrieveConnectionParameters() {
        final @ConnectionType.Type int connectionType = dpPrefs.getConnectionParameterType();

        // Generate the uri for logging the tlog data for this session.
        Uri tlogLoggingUri = TLogUtils.getTLogLoggingUri(getApplicationContext(),
                connectionType, System.currentTimeMillis());

        ConnectionParameter connParams;
        switch (connectionType) {
            case ConnectionType.TYPE_USB:
                connParams = ConnectionParameter.newUsbConnection(dpPrefs.getUsbBaudRate(),
                        tlogLoggingUri, EVENTS_DISPATCHING_PERIOD);
                break;

            case ConnectionType.TYPE_UDP:
                if (dpPrefs.isUdpPingEnabled()) {
                    connParams = ConnectionParameter.newUdpWithPingConnection(
                            dpPrefs.getUdpServerPort(),
                            dpPrefs.getUdpPingReceiverIp(),
                            dpPrefs.getUdpPingReceiverPort(),
                            "Hello".getBytes(),
                            ConnectionType.DEFAULT_UDP_PING_PERIOD,
                            tlogLoggingUri,
                            EVENTS_DISPATCHING_PERIOD);
                }
                else{
                    connParams = ConnectionParameter.newUdpConnection(dpPrefs.getUdpServerPort(),
                            tlogLoggingUri, EVENTS_DISPATCHING_PERIOD);
                }
                break;

            case ConnectionType.TYPE_TCP:
                connParams = ConnectionParameter.newTcpConnection(dpPrefs.getTcpServerIp(),
                        dpPrefs.getTcpServerPort(), tlogLoggingUri, EVENTS_DISPATCHING_PERIOD);
                break;

            case ConnectionType.TYPE_BLUETOOTH:
                String btAddress = dpPrefs.getBluetoothDeviceAddress();
                if (TextUtils.isEmpty(btAddress)) {
                    connParams = null;
                    startActivity(new Intent(getApplicationContext(),
                            BluetoothDevicesActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

                } else {
                    connParams = ConnectionParameter.newBluetoothConnection(btAddress,
                            tlogLoggingUri, EVENTS_DISPATCHING_PERIOD);
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
    public void onDroneEvent(String event, Bundle extras) {
        switch (event) {
            case AttributeEvent.STATE_CONNECTED: {
                handler.removeCallbacks(disconnectionTask);

                startDroneSession(System.currentTimeMillis());

                startService(new Intent(getApplicationContext(), AppService.class));

                final boolean isReturnToMeOn = dpPrefs.isReturnToMeEnabled();
                VehicleApi.getApi(drone).enableReturnToMe(isReturnToMeOn, new AbstractCommandListener() {
                    @Override
                    public void onSuccess() {
                        Timber.i("Return to me %s successfully.", isReturnToMeOn ? "started" : "stopped");
                    }

                    @Override
                    public void onError(int i) {
                        Timber.e("%s return to me failed: %d", isReturnToMeOn ? "Starting" : "Stopping", i);
                    }

                    @Override
                    public void onTimeout() {
                        Timber.w("%s return to me timed out.", isReturnToMeOn ? "Starting" : "Stopping");
                    }
                });

                final Intent droneIntent = new Intent(event);
                if (extras != null)
                    droneIntent.putExtras(extras);
                lbm.sendBroadcast(droneIntent);
                break;
            }

            case AttributeEvent.STATE_DISCONNECTED: {
                shouldWeTerminate();

                final Intent droneIntent = new Intent(event);
                if (extras != null)
                    droneIntent.putExtras(extras);
                lbm.sendBroadcast(droneIntent);

                endDroneSession();
                // Fire the droneshare log uploader
                UploaderService.kickStart(getApplicationContext());
                break;
            }

            case AttributeEvent.PARAMETERS_REFRESH_COMPLETED:
                // Grab the vehicle default speed, and update the preferences.
                double speedParameter = drone.getSpeedParameter() / 100; //cm/s to m/s conversion.
                if (speedParameter != 0) {
                    dpPrefs.setVehicleDefaultSpeed((float) speedParameter);
                }
                // FALL THROUGH

            default: {
                final Intent droneIntent = new Intent(event);
                if (extras != null)
                    droneIntent.putExtras(extras);
                lbm.sendBroadcast(droneIntent);
                break;
            }
        }
    }

    @Override
    public void onDroneServiceInterrupted(String errorMsg) {
        Timber.d("Drone service interrupted: %s", errorMsg);
        controlTower.unregisterDrone(drone);

        if (!TextUtils.isEmpty(errorMsg))
            Log.e(TAG, errorMsg);
    }

    public static void setCellularNetworkAvailability(boolean isAvailable){
        isCellularNetworkOn.set(isAvailable);
    }

    public static boolean isCellularNetworkAvailable(){
        return isCellularNetworkOn.get();
    }

    public void createFileStartLogging() {
        if (logToFileTree != null) {
            logToFileTree.createFileStartLogging(getApplicationContext());
        }
    }

    public void closeLogFile() {
        if(logToFileTree != null) {
            logToFileTree.stopLoggingThread();
        }
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    private void startDroneSession(long startTime) {
        ConnectionParameter connParams = drone.getConnectionParameter();
        @ConnectionType.Type int connectionType = connParams.getConnectionType();
        final String connectionTypeLabel = ConnectionType.getConnectionTypeLabel(connectionType);
        Uri tlogLoggingUri = connParams.getTLogLoggingUri();

        // Record the starting drone session
        currentSessionId = this.sessionDB.startSession(startTime, connectionTypeLabel, tlogLoggingUri);
        if(tlogLoggingUri != null && dpPrefs.isDroneshareEnabled()){
            //Create an entry in the droneshare upload queue
            droneShareDb.queueDataUploadEntry(dpPrefs.getDroneshareLogin(), currentSessionId);
        }
    }

    private void endDroneSession() {
        //log into the database the disconnection time.
        if(currentSessionId != INVALID_SESSION_ID) {
            this.sessionDB.endSessions(System.currentTimeMillis(), currentSessionId);
        }
    }

    private void cleanupDroneSessions(){
        //Cleanup all the opened drone sessions
        sessionDB.cleanupOpenedSessions(System.currentTimeMillis());

        // Check for droneshare logs to upload.
        UploaderService.kickStart(getApplicationContext());
    }

    public DroneShareDB getDroneShareDatabase(){
        return droneShareDb;
    }

    /** Return the vehicle speed in meters per second. */
    public double getVehicleSpeed() {
        double speedParameter = drone.getSpeedParameter() / 100; //cm/s to m/s conversion.
        if (speedParameter == 0) {
            speedParameter = dpPrefs.getVehicleDefaultSpeed();
        }
        return speedParameter;
    }

    public SessionDB getSessionDatabase(){
        return sessionDB;
    }
}