package org.droidplanner.android.services;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.MAVLink.Messages.ApmModes;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.droidplanner.android.gcs.follow.Follow;
import org.droidplanner.android.lib.utils.GoogleApiClientManager;
import org.droidplanner.android.lib.utils.WearUtils;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;

/**
 * Manages communication with the wearable app.
 */
public class WearManagerService extends WearableListenerService implements OnDroneListener {

    private final static String TAG = WearManagerService.class.getSimpleName();

    /**
     * Used to retrieve a DroidPlannerApi object handle.
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mDpApi = (DroidPlannerService.DroidPlannerApi) service;

            mDrone = mDpApi.getDrone();
            mDrone.events.addDroneListener(WearManagerService.this);

            mFollowMe = mDpApi.getFollowMe();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mDpApi = null;
        }
    };

    /**
     * Contains current drone state information that's relevant to the connected wear node(s).
     */
    private final Bundle mDroneInfoBundle = new Bundle();

    /**
     * Handle to the droidplanner api.
     */
    private DroidPlannerService.DroidPlannerApi mDpApi;

    /**
     * Represents the drone controlled by the app.
     */
    private Drone mDrone;

    /**
     * Handle to toggle follow me mode.
     */
    private Follow mFollowMe;

    /**
     * Manager for the google api client. Handles connection/disconnection and running of
     * google api client related tasks.
     */
    private GoogleApiClientManager mGApiClientMgr;

    @Override
    public void onCreate() {
        super.onCreate();
        mGApiClientMgr = new GoogleApiClientManager(getApplicationContext(), Wearable.API);
        mGApiClientMgr.start();

        bindService(new Intent(getApplicationContext(), DroidPlannerService.class),
                mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbindService(mServiceConnection);

        if (mDrone != null) {
            mDrone.events.removeDroneListener(this);
        }

        mGApiClientMgr.stop();
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        boolean relayInfo = true;
        switch (event) {
            case CONNECTED:
            case DISCONNECTED:
                mDroneInfoBundle.putBoolean(WearUtils.KEY_DRONE_CONNECTION_STATE,
                        drone.MavClient.isConnected());
                break;

            case FOLLOW_CHANGE_TYPE:
            case FOLLOW_START:
                mDroneInfoBundle.putBoolean(WearUtils.KEY_DRONE_FOLLOW_STATE,
                        mFollowMe.isEnabled());
                break;

            case MODE:
            case TYPE:
                mDroneInfoBundle.putString(WearUtils.KEY_DRONE_FLIGHT_MODE,
                        drone.state.getMode().getName());
                mDroneInfoBundle.putInt(WearUtils.KEY_DRONE_TYPE, drone.type.getType());
                break;

            case HOME:
                mDroneInfoBundle.putString(WearUtils.KEY_DRONE_HOME,
                        drone.home.getDroneDistanceToHome().toString());
                break;

            case STATE:
                mDroneInfoBundle.putLong(WearUtils.KEY_DRONE_FLIGHT_TIME,
                        drone.state.getFlightTime());
                break;

            case RADIO:
                mDroneInfoBundle.putInt(WearUtils.KEY_DRONE_SIGNAL, drone.radio.getSignalStrength
                        ());
                break;

            case BATTERY:
                mDroneInfoBundle.putString(WearUtils.KEY_DRONE_BATTERY, drone.battery.toString());
                break;

            case GPS_COUNT:
            case GPS_FIX:
                mDroneInfoBundle.putString(WearUtils.KEY_DRONE_GPS, drone.GPS.toString());
                break;

            case ORIENTATION:
                mDroneInfoBundle.putDouble(WearUtils.KEY_DRONE_ROLL, drone.orientation.getRoll());
                mDroneInfoBundle.putDouble(WearUtils.KEY_DRONE_PITCH, drone.orientation.getPitch());
                mDroneInfoBundle.putDouble(WearUtils.KEY_DRONE_YAW, drone.orientation.getYaw());
                break;

            case SPEED:
                mDroneInfoBundle.putDouble(WearUtils.KEY_DRONE_AIR_SPEED,
                        drone.speed.getAirSpeed());
                mDroneInfoBundle.putDouble(WearUtils.KEY_DRONE_GROUND_SPEED,
                        drone.speed.getGroundSpeed());
                mDroneInfoBundle.putDouble(WearUtils.KEY_DRONE_CLIMB_RATE,
                        drone.speed.getVerticalSpeed());
                mDroneInfoBundle.putDouble(WearUtils.KEY_DRONE_ALTITUDE,
                        drone.altitude.getAltitude());
                break;

            default:
                relayInfo = false;
                break;
        }

        if (relayInfo) {
            relayDroneInfo();
        }
    }

    private void relayDroneInfo() {
        boolean result = WearUtils.asyncPutDataItem(mGApiClientMgr, WearUtils.DRONE_INFO_PATH,
                mDroneInfoBundle);

        if (!result) {
            Log.e(TAG, "Unable to add google api client task.");
        }
    }

    @Override
    public void onMessageReceived(MessageEvent msgEvent) {
        final String msgPath = msgEvent.getPath();
        if (WearUtils.RESET_DRONE_FLIGHT_TIME_PATH.equals(msgPath)) {
            mDrone.state.resetFlightTimer();
        }
        else if (WearUtils.TOGGLE_DRONE_FOLLOW_ME_PATH.equals(msgPath)) {
            boolean newState = WearUtils.decodeFollowMeMsgData(msgEvent.getData());
            if (mFollowMe.isEnabled() != newState) {
                mFollowMe.toggleFollowMeState();
            }
        }
        else if (WearUtils.SET_DRONE_FLIGHT_MODE_PATH.equals(msgPath)) {
            ApmModes flightMode = WearUtils.decodeFlightModeMsgData(msgEvent.getData());
            mDrone.state.setMode(flightMode);
        }
        else if (WearUtils.TOGGLE_DRONE_CONNECTION_PATH.equals(msgPath) && mDpApi != null) {

            boolean shouldConnect = WearUtils.decodeDroneConnectionMsgData(msgEvent.getData());
            if (mDpApi.isDroneConnected() != shouldConnect && !mDpApi.toggleDroneConnection()) {

                //Have the wear node(s) tell the user to check the main app.
                boolean result = WearUtils.asyncSendMessage(mGApiClientMgr,
                        WearUtils.PHONE_USE_REQUIRED_PATH, null);

                if (!result) {
                    Log.e(TAG, "Unable to add google api client task.");
                }
            }
        }
    }
}
