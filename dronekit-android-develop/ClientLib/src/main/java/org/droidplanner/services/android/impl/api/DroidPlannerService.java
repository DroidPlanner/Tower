package org.droidplanner.services.android.impl.api;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.o3dr.android.client.R;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.mission.item.complex.CameraDetail;
import com.o3dr.services.android.lib.model.IApiListener;
import com.o3dr.services.android.lib.model.IDroidPlannerServices;

import org.droidplanner.services.android.impl.core.drone.DroneManager;
import org.droidplanner.services.android.impl.core.survey.CameraInfo;
import org.droidplanner.services.android.impl.utils.Utils;
import org.droidplanner.services.android.impl.utils.file.IO.CameraInfoLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import timber.log.Timber;

/**
 * DroneKit-Android background service implementation.
 */
public class DroidPlannerService extends Service {

        /**
     * Status bar notification id
     */
    private static final int FOREGROUND_ID = 101;

    /**
     * Set of actions to notify the local app's components of the service events.
     */
    public static final String ACTION_DRONE_CREATED = Utils.PACKAGE_NAME + ".ACTION_DRONE_CREATED";
    public static final String ACTION_DRONE_DESTROYED = Utils.PACKAGE_NAME + ".ACTION_DRONE_DESTROYED";
    public static final String ACTION_RELEASE_API_INSTANCE = Utils.PACKAGE_NAME + ".action.RELEASE_API_INSTANCE";
    public static final String EXTRA_API_INSTANCE_APP_ID = "extra_api_instance_app_id";

    /**
     * Used to broadcast service events.
     */
    private LocalBroadcastManager lbm;

    /**
     * Stores drone api instances per connected client. The client are denoted by their app id.
     */
    final ConcurrentHashMap<String, DroneApi> droneApiStore = new ConcurrentHashMap<>();

    /**
     * Caches drone managers per connection type.
     */
    final ConcurrentHashMap<ConnectionParameter, DroneManager> droneManagers = new ConcurrentHashMap<>();

    private DPServices dpServices;

    private CameraInfoLoader cameraInfoLoader;
    private List<CameraDetail> cachedCameraDetails;

    /**
     * Generate a drone api instance for the client denoted by the given app id.
     *
     * @param listener Used to retrieve api information.
     * @param appId    Application id of the connecting client.
     * @return a IDroneApi instance
     */
    DroneApi registerDroneApi(IApiListener listener, String appId) {
        if (listener == null)
            return null;

        DroneApi droneApi = new DroneApi(this, listener, appId);
        droneApiStore.put(appId, droneApi);
        lbm.sendBroadcast(new Intent(ACTION_DRONE_CREATED));
        updateForegroundNotification();
        return droneApi;
    }

    /**
     * Release the drone api instance attached to the given app id.
     *
     * @param appId Application id of the disconnecting client.
     */
    void releaseDroneApi(String appId) {
        if (appId == null)
            return;

        DroneApi droneApi = droneApiStore.remove(appId);
        if (droneApi != null) {
            Timber.d("Releasing drone api instance for " + appId);
            droneApi.destroy();
            lbm.sendBroadcast(new Intent(ACTION_DRONE_DESTROYED));
            updateForegroundNotification();
        }
    }

    /**
     * Establish a connection with a vehicle using the given connection parameter.
     *
     * @param connParams Parameters used to connect to the vehicle.
     * @param appId      Application id of the connecting client.
     * @param listener   Callback to receive drone events.
     * @return A DroneManager instance which acts as router between the connected vehicle and the listeneing client(s).
     */
    DroneManager connectDroneManager(ConnectionParameter connParams, String appId, DroneApi listener) {
        if (connParams == null || TextUtils.isEmpty(appId) || listener == null)
            return null;

        DroneManager droneMgr = droneManagers.get(connParams);
        if (droneMgr == null) {
            final DroneManager temp = DroneManager.generateDroneManager(getApplicationContext(), connParams, new Handler(Looper.getMainLooper()));

            droneMgr = droneManagers.putIfAbsent(connParams, temp);
            if(droneMgr == null){
                Timber.d("Generating new drone manager.");
                droneMgr = temp;
            }
            else{
                temp.destroy();
            }
        }

        Timber.d("Drone manager connection for " + appId);
        droneMgr.connect(appId, listener, connParams);
        return droneMgr;
    }

    /**
     * Disconnect the given client from the vehicle managed by the given drone manager.
     *
     * @param droneMgr   Handler for the connected vehicle.
     * @param clientInfo Info of the disconnecting client.
     */
    void disconnectDroneManager(DroneManager droneMgr, DroneApi.ClientInfo clientInfo) {
        if (droneMgr == null || clientInfo == null || TextUtils.isEmpty(clientInfo.appId))
            return;

        String appId = clientInfo.appId;
        Timber.d("Drone manager disconnection for " + appId);
        droneMgr.disconnect(clientInfo);
        if (droneMgr.getConnectedAppsCount() == 0) {
            Timber.d("Destroying drone manager.");
            droneMgr.destroy();
            droneManagers.remove(droneMgr.getConnectionParameter());
        }
    }

    /**
     * Retrieves the set of camera info provided by the app.
     *
     * @return a list of {@link CameraDetail} objects.
     */
    synchronized List<CameraDetail> getCameraDetails() {
        if (cachedCameraDetails == null) {
            List<String> cameraInfoNames = cameraInfoLoader.getCameraInfoList();

            List<CameraInfo> cameraInfos = new ArrayList<>(cameraInfoNames.size());
            for (String infoName : cameraInfoNames) {
                try {
                    cameraInfos.add(cameraInfoLoader.openFile(infoName));
                } catch (Exception e) {
                    Timber.e(e, e.getMessage());
                }
            }

            List<CameraDetail> cameraDetails = new ArrayList<>(cameraInfos.size());
            for (CameraInfo camInfo : cameraInfos) {
                cameraDetails.add(new CameraDetail(camInfo.name, camInfo.sensorWidth,
                        camInfo.sensorHeight, camInfo.sensorResolution, camInfo.focalLength,
                        camInfo.overlap, camInfo.sidelap, camInfo.isInLandscapeOrientation));
            }

            cachedCameraDetails = cameraDetails;
        }

        return cachedCameraDetails;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Timber.d("Binding intent: " + intent);
        final String action = intent.getAction();
        if (IDroidPlannerServices.class.getName().equals(action)) {
            // Return binder to ipc client-server interaction.
            return dpServices;
        } else {
            return null;
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onCreate() {
        super.onCreate();

        Timber.d("Creating DroneKit-Android.");

        final Context context = getApplicationContext();

        dpServices = new DPServices(this);
        lbm = LocalBroadcastManager.getInstance(context);
        this.cameraInfoLoader = new CameraInfoLoader(context);

        updateForegroundNotification();
    }

    @SuppressLint("NewApi")
    private void updateForegroundNotification() {
        final Context context = getApplicationContext();

        //Put the service in the foreground
        final NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context)
                .setContentTitle("DroneKit-Android")
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setSmallIcon(R.drawable.ic_stat_notify);

        final int connectedCount = droneApiStore.size();
        if (connectedCount > 1) {
            notifBuilder.setContentText(connectedCount + " connected apps");
        }

        final Notification notification = notifBuilder.build();
        startForeground(FOREGROUND_ID, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.d("Destroying DroneKit-Android.");

        for (DroneApi droneApi : droneApiStore.values()) {
            droneApi.destroy();
        }
        droneApiStore.clear();

        for (DroneManager droneMgr : droneManagers.values()) {
            droneMgr.destroy();
        }
        droneManagers.clear();

        dpServices.destroy();

        stopForeground(true);

        //Disable this service. It'll be reenabled the next time its local client needs it.
        enableDroidPlannerService(getApplicationContext(), false);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            switch (action) {

                case ACTION_RELEASE_API_INSTANCE:
                    final String appId = intent.getStringExtra(EXTRA_API_INSTANCE_APP_ID);
                    releaseDroneApi(appId);
                    break;
            }
        }

        stopSelf();
        return START_NOT_STICKY;
    }

    /**
     * Toggles the DroidPlannerService component
     * @param context
     * @param enable
     */
    public static void enableDroidPlannerService(Context context, boolean enable){
        final ComponentName serviceComp = new ComponentName(context, DroidPlannerService.class);
        final int newState = enable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

        context.getPackageManager().setComponentEnabledSetting(serviceComp, newState, PackageManager.DONT_KILL_APP);
    }

}
