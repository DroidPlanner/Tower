package org.droidplanner.android.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.activities.helpers.BluetoothDevicesActivity;
import org.droidplanner.android.gcs.follow.Follow;
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

    /**
     * Handle to the droidplanner api, provided to clients of this service.
     */
    private final IBinder mBinder = new DroidPlannerApi();

    /**
     * Handle to the app preferences.
     */
    private DroidPlannerPrefs mAppPrefs;

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
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mDrone.events.removeDroneListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {

    }

    /**
     * Provide access to droidplanner set of apis.
     */
    public class DroidPlannerApi extends Binder {

        public Drone getDrone(){
            return mDrone;
        }

        public Follow getFollowMe(){
            return mFollowMe;
        }

        public void toggleDroneConnection(){
            if (!mDrone.MavClient.isConnected()) {
                final String connectionType = mAppPrefs.getMavLinkConnectionType();

                if (Utils.ConnectionType.BLUETOOTH.name().equals(connectionType)) {
                    // Launch a bluetooth device selection screen for the user
                    final String address = mAppPrefs.getBluetoothDeviceAddress();
                    if(address == null || address.isEmpty()) {
                        startActivity(new Intent(getApplicationContext(),
                                BluetoothDevicesActivity.class).addFlags(Intent
                                .FLAG_ACTIVITY_NEW_TASK));
                        return;
                    }
                }
            }
            mDrone.MavClient.toggleConnectionState();
        }

        public boolean isDroneConnected(){
            return mDrone.MavClient.isConnected();
        }
    }
}
