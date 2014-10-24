package org.droidplanner.android.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;

import com.MAVLink.Messages.MAVLinkMessage;

import org.droidplanner.android.activities.helpers.BluetoothDevicesActivity;
import org.droidplanner.android.communication.service.MAVLinkClient;
import org.droidplanner.android.gcs.location.FusedLocation;
import org.droidplanner.android.notifications.NotificationHandler;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.utils.Utils;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.core.MAVLink.MAVLinkStreams;
import org.droidplanner.core.MAVLink.MavLinkMsgHandler;
import org.droidplanner.core.drone.DroneImpl;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.gcs.follow.Follow;
import org.droidplanner.core.model.Drone;

import java.lang.ref.WeakReference;

/**
 * DroidPlanner's background service.
 * Handles communication and interaction with the drone.
 */
public class DroidPlannerService extends Service implements MAVLinkStreams.MavlinkInputStream,
        DroneInterfaces.OnDroneListener{

    public final static String ACTION_TOGGLE_DRONE_CONNECTION = DroidPlannerService.class.getName()
            + ".ACTION_TOGGLE_DRONE_CONNECTION";

    private final Handler handler = new Handler();
    private final DroidPlannerApi dpApi = new DroidPlannerApi(this);

    private Drone drone;
    private Follow followMe;
    private MissionProxy missionProxy;
    private DroidPlannerPrefs appPrefs;
    private MavLinkMsgHandler mavLinkMsgHandler;
    private NotificationHandler notificationHandler;

    @Override
    public IBinder onBind(Intent intent) {
        return dpApi;
    }

    @Override
    public void onCreate(){
        super.onCreate();

        final DroneInterfaces.Clock clock = new DroneInterfaces.Clock() {
            @Override
            public long elapsedRealtime() {
                return SystemClock.elapsedRealtime();
            }
        };

        final Context context = getApplicationContext();
        final DroneInterfaces.Handler droneHandler = new DroneInterfaces.Handler() {
            @Override
            public void removeCallbacks(Runnable thread) {
                handler.removeCallbacks(thread);
            }

            @Override
            public void post(Runnable thread) {
                handler.post(thread);
            }

            @Override
            public void postDelayed(Runnable thread, long timeout) {
                handler.postDelayed(thread, timeout);
            }
        };

        appPrefs = new DroidPlannerPrefs(context);
        drone = new DroneImpl(new MAVLinkClient(context, this), clock, droneHandler, appPrefs);
        mavLinkMsgHandler = new MavLinkMsgHandler(drone);
        missionProxy = new MissionProxy(drone.getMission());
        followMe = new Follow(drone, droneHandler, new FusedLocation(context));
        notificationHandler = new NotificationHandler(context);

        drone.addDroneListener(this);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        drone.removeDroneListener(this);
        notificationHandler.terminate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        if(intent != null){
            final String action = intent.getAction();
            if(ACTION_TOGGLE_DRONE_CONNECTION.equals(action)){
                dpApi.toggleDroneConnection();
            }
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public void notifyConnected() {
        drone.notifyDroneEvent(DroneInterfaces.DroneEventsType.CONNECTED);
    }

    @Override
    public void notifyDisconnected() {
        drone.notifyDroneEvent(DroneInterfaces.DroneEventsType.DISCONNECTED);
    }

    @Override
    public void notifyReceivedData(MAVLinkMessage m) {
        mavLinkMsgHandler.receiveData(m);
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        notificationHandler.onDroneEvent(event, drone);

        switch(event){
            case MISSION_RECEIVED:
                //Refresh the mission state
                missionProxy.refresh();
                break;

            default:
                break;

        }
    }

    public static class DroidPlannerApi extends Binder {

        private final WeakReference<DroidPlannerService> serviceRef;

        private DroidPlannerApi(DroidPlannerService service){
            this.serviceRef = new WeakReference<DroidPlannerService>(service);
        }

        private DroidPlannerService getService(){
            final DroidPlannerService service = this.serviceRef.get();
            if(service == null)
                throw new IllegalStateException("Lost reference to the parent service.");

            return service;
        }

        public Drone getDrone(){
            return getService().drone;
        }

        public Follow getFollowMe(){
            return getService().followMe;
        }

        public MissionProxy getMissionProxy(){
            return getService().missionProxy;
        }

        private void toggleDroneConnection(){
            final DroidPlannerService service = getService();
            final Drone drone = service.drone;
            final DroidPlannerPrefs prefs = service.appPrefs;

            if (!drone.getMavClient().isConnected()) {
                final String connectionType = prefs.getMavLinkConnectionType();

                if (Utils.ConnectionType.BLUETOOTH.name().equals(connectionType)) {
                    // Launch a bluetooth device selection screen for the user
                    final String address = prefs.getBluetoothDeviceAddress();
                    if (address == null || address.isEmpty()) {
                        service.startActivity(
                                new Intent(service.getApplicationContext(),
                                        BluetoothDevicesActivity.class)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        return;
                    }
                }
            }
            drone.getMavClient().toggleConnectionState();
        }
    }
}
