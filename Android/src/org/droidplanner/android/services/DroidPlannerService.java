package org.droidplanner.android.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.MAVLink.Messages.ApmModes;

import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.activities.helpers.BluetoothDevicesActivity;
import org.droidplanner.android.gcs.follow.Follow;
import org.droidplanner.android.notifications.NotificationHandler;
import org.droidplanner.android.utils.Utils;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces;

/**
 * This is DroidPlanner's background service. It's goal is to manage communication,
 * and event handling of drone related events, and have the ui components retrieved data as
 * needed via binder.
 */
public class DroidPlannerService extends Service implements DroneInterfaces.OnDroneListener {

    public final static String ACTION_TOGGLE_DRONE_CONNECTION = DroidPlannerService.class
            .getName() + ".ACTION_TOGGLE_DRONE_CONNECTION";

    /**
     * Used to execute tasks on the main thread.
     */
    private final Handler mHandler = new Handler();

    /**
     * Handle to the droidplanner api, provided to clients of this service.
     */
    private final DroidPlannerApi mDpApi = new DroidPlannerApi();

    /**
     * Handle to the app preferences.
     */
    private DroidPlannerPrefs mAppPrefs;

    /**
     * Handles dispatching of status bar, and audible notification.
     */
    public NotificationHandler mNotificationHandler;

    /**
     * Represents the drone controlled by the app.
     */
    private Drone mDrone;

    /**
     * Handle to toggle follow me mode.
     */
    private Follow mFollowMe;

    @Override
    public void onCreate(){
        super.onCreate();

        final DroidPlannerApp dpApp = (DroidPlannerApp) getApplication();
        mFollowMe = dpApp.followMe;

        mDrone = dpApp.getDrone();
        mDrone.events.addDroneListener(this);

        mAppPrefs = new DroidPlannerPrefs(getApplicationContext());

        mNotificationHandler = new NotificationHandler(dpApp);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mDrone.events.removeDroneListener(this);
        mNotificationHandler.terminate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        if(intent != null) {
            final String action = intent.getAction();
            if(ACTION_TOGGLE_DRONE_CONNECTION.equals(action)){
                mDpApi.toggleDroneConnection();
            }
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mDpApi;
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        switch(event){
            case CONNECTED:
                break;

            case DISCONNECTED:
                stopSelf();
                break;
        }
    }

    /**
     * Provide access to droidplanner set of apis.
     */
    public class DroidPlannerApi extends Binder {

        /**
         * Toggle the connection with the drone.
         * @return true to indicate the operation is in process,
         * false to indicate that additional user interaction is needed.
         */
        public boolean toggleDroneConnection(){
            if (!mDrone.MavClient.isConnected()) {
                final String connectionType = mAppPrefs.getMavLinkConnectionType();

                if (Utils.ConnectionType.BLUETOOTH.name().equals(connectionType)) {
                    // Launch a bluetooth device selection screen for the user
                    final String address = mAppPrefs.getBluetoothDeviceAddress();
                    if(address == null || address.isEmpty()) {
                        startActivity(new Intent(getApplicationContext(),
                                BluetoothDevicesActivity.class).addFlags(Intent
                                .FLAG_ACTIVITY_NEW_TASK));
                        return false;
                    }
                }
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDrone.MavClient.toggleConnectionState();
                }
            });
            return true;
        }

        public boolean isFollowMeEnabled(){
            return mFollowMe.isEnabled();
        }

        public void setFlightMode(final ApmModes flightMode){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDrone.state.changeFlightMode(flightMode);
                }
            });
        }

        public void toggleFollowMe(){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mFollowMe.toggleFollowMeState();
                }
            });
        }

        public void resetFlightTimer(){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDrone.state.resetFlightTimer();
                }
            });
        }

        public boolean isDroneConnected(){
            return mDrone.MavClient.isConnected();
        }

        public void quickNotify(String msg){
            mNotificationHandler.quickNotify(msg);
        }

        public void queryConnectionState(){
            mDrone.MavClient.queryConnectionState();
        }
    }
}
