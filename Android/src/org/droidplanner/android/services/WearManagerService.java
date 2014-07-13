package org.droidplanner.android.services;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.MAVLink.Messages.ApmModes;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.droidplanner.android.gcs.follow.Follow;
import org.droidplanner.android.lib.utils.WearUtils;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages communication with the wearable app.
 */
public class WearManagerService extends WearableListenerService implements GoogleApiClient
        .ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        DroneInterfaces.OnDroneListener {

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
     * Stores the list of connected wear nodes.
     * If no nodes is connected, this service stops updating the data items.
     */
    private final Map<String, Node> mConnectedNodes = new HashMap<String, Node>();

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

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        bindService(new Intent(getApplicationContext(), DroidPlannerService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbindService(mServiceConnection);

        if(mDrone != null) {
            mDrone.events.removeDroneListener(this);
        }

        if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        relayDroneInfo();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "Google Play Services connection suspended (" + i + ").");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Google Play Services connection failed (" + connectionResult.getErrorCode() +
                ").");
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
        if (!mGoogleApiClient.isConnected()) {
            Log.w(TAG, "The google api client is not connected. Cancelling relay operation.");
            return;
        }

        final PutDataMapRequest dataMap = PutDataMapRequest.create(WearUtils.DRONE_INFO_PATH);
        dataMap.getDataMap().putAll(DataMap.fromBundle(mDroneInfoBundle));
        PutDataRequest request = dataMap.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        final Status status = dataItemResult.getStatus();
                        if (!status.isSuccess()) {
                            Log.e(TAG, "Failed to relay the data: " + status.getStatusCode());
                        }
                    }
                });
    }

    @Override
    public void onMessageReceived(MessageEvent msgEvent) {
        final String msgPath = msgEvent.getPath();
        if(WearUtils.RESET_DRONE_FLIGHT_TIME_PATH.equals(msgPath)){
            mDrone.state.resetFlightTimer();
        }
        else if(WearUtils.TOGGLE_DRONE_CONNECTION_PATH.equals(msgPath)){
            if(mDpApi != null) {
                boolean shouldConnect = WearUtils.decodeDroneConnectionMsgData(msgEvent.getData());
                if(mDpApi.isDroneConnected() != shouldConnect){
                    if(!mDpApi.toggleDroneConnection()){
                        //Have the wear node(s) tell the user to check the main app.
                        if(!mGoogleApiClient.isConnected()){
                            Log.w(TAG, "The google api client is not connected. Cancelling relay operation.");
                            return;
                        }

                        for(String nodeId: mConnectedNodes.keySet()) {
                            Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeId,
                                    WearUtils.PHONE_USE_REQUIRED_PATH, null)
                                    .setResultCallback(new ResultCallback<MessageApi
                                            .SendMessageResult>() {

                                        @Override
                                        public void onResult(MessageApi.SendMessageResult
                                                                     sendMessageResult) {
                                            final Status status = sendMessageResult.getStatus();
                                            if (!status.isSuccess()) {
                                                Log.e(TAG, "Failed to relay the data: " + status
                                                        .getStatusCode());
                                            }
                                        }
                                    });
                        }
                    }
                }
            }
        }
        else if(WearUtils.TOGGLE_DRONE_FOLLOW_ME_PATH.equals(msgPath)){
            boolean newState = WearUtils.decodeFollowMeMsgData(msgEvent.getData());
            if(mFollowMe.isEnabled() != newState){
                mFollowMe.toggleFollowMeState();
            }
        }
        else if(WearUtils.SET_DRONE_FLIGHT_MODE_PATH.equals(msgPath)){
            ApmModes flightMode = WearUtils.decodeFlightModeMsgData(msgEvent.getData());
            mDrone.state.setMode(flightMode);
        }
    }

    @Override
    public void onPeerConnected(Node peer) {
        mConnectedNodes.put(peer.getId(), peer);
        if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        mConnectedNodes.remove(peer.getId());
        if (mConnectedNodes.isEmpty()) {
            mGoogleApiClient.disconnect();
        }
    }
}
