package org.droidplanner.android.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.droidplanner.android.gcs.follow.Follow;
import org.droidplanner.android.lib.utils.WearUtils;
import org.droidplanner.android.services.WearNotificationService;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces;

/**
 * Relays drone data to the connected wear nodes.
 */
public class WearNotificationProvider implements NotificationHandler.NotificationProvider{

    private final static String TAG = WearNotificationProvider.class.getSimpleName();

    /**
     * Contains current drone state information that's relevant to the connected wear node(s).
     */
    private final Bundle mDroneInfoBundle = new Bundle();

    /**
     * Application context
     */
    private final Context mContext;

    private final Follow mFollowMe;

    WearNotificationProvider(Context context, Follow followMe){
        mContext = context;
        mFollowMe = followMe;
    }

    @Override
    public void quickNotify(String feedback) {

    }

    @Override
    public void onTerminate() {
        mDroneInfoBundle.clear();
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
                mDroneInfoBundle.putInt(WearUtils.KEY_DRONE_SIGNAL, drone.radio.getSignalStrength());
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
        mContext.startService(new Intent(mContext, WearNotificationService.class).setAction(WearUtils
                .DRONE_INFO_PATH).putExtra(WearUtils.DRONE_INFO_PATH, mDroneInfoBundle));
    }
}
