package org.droidplanner.android.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.droidplanner.android.gcs.follow.Follow;
import org.droidplanner.android.lib.parcelables.ParcelableApmMode;
import org.droidplanner.android.lib.parcelables.ParcelableBattery;
import org.droidplanner.android.lib.parcelables.ParcelableGPS;
import org.droidplanner.android.lib.parcelables.ParcelableOrientation;
import org.droidplanner.android.lib.parcelables.ParcelableRadio;
import org.droidplanner.android.lib.parcelables.ParcelableSpeed;
import org.droidplanner.android.lib.utils.ParcelableUtils;
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
        mContext.startService(new Intent(mContext, WearNotificationService.class).setAction
                (WearUtils.MAIN_APP_STARTED_PATH));
    }

    @Override
    public void quickNotify(String feedback) {

    }

    @Override
    public void onTerminate() {
        mContext.startService(new Intent(mContext, WearNotificationService.class).setAction
                (WearUtils.MAIN_APP_STOPPED_PATH));
        mDroneInfoBundle.clear();
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        boolean relayInfo = true;
        switch (event) {
            case CONNECTED:
            case DISCONNECTED:
                relayConnectionState(drone.MavClient.isConnected());
                return;

            case FOLLOW_CHANGE_TYPE:
            case FOLLOW_START:
                mDroneInfoBundle.putBoolean(WearUtils.KEY_DRONE_FOLLOW_STATE,
                        mFollowMe.isEnabled());
                break;

            case MODE:
            case TYPE:
                mDroneInfoBundle.putByteArray(WearUtils.KEY_DRONE_FLIGHT_MODE,
                        ParcelableUtils.marshall(new ParcelableApmMode(drone.state.getMode())));
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
                mDroneInfoBundle.putByteArray(WearUtils.KEY_DRONE_SIGNAL,
                        ParcelableUtils.marshall(new ParcelableRadio(drone.radio)));
                break;

            case BATTERY:
                mDroneInfoBundle.putByteArray(WearUtils.KEY_DRONE_BATTERY,
                        ParcelableUtils.marshall(new ParcelableBattery(drone.battery)));
                break;

            case GPS_COUNT:
            case GPS_FIX:
                mDroneInfoBundle.putByteArray(WearUtils.KEY_DRONE_GPS,
                        ParcelableUtils.marshall(new ParcelableGPS(drone.GPS)));
                break;

            case ORIENTATION:
                mDroneInfoBundle.putByteArray(WearUtils.KEY_DRONE_ORIENTATION,
                        ParcelableUtils.marshall(new ParcelableOrientation(drone.orientation)));
                break;

            case SPEED:
                mDroneInfoBundle.putByteArray(WearUtils.KEY_DRONE_SPEED,
                        ParcelableUtils.marshall(new ParcelableSpeed(drone.speed)));
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

    private void relayConnectionState(boolean isConnected){
        mContext.startService(new Intent(mContext, WearNotificationService.class).setAction
                (WearUtils.DRONE_CONNECTION_PATH).putExtra(WearUtils.DRONE_CONNECTION_PATH,
                isConnected));
    }
}
