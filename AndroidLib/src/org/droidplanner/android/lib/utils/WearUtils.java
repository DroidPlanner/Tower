package org.droidplanner.android.lib.utils;

import android.os.Bundle;
import android.util.Log;

import com.MAVLink.Messages.ApmModes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * Contains functions and constants related to the wear data item api.
 */
public class WearUtils {

    private static final String TAG = WearUtils.class.getSimpleName();

    //Not instantiable
    private WearUtils() {}

    /*
    Data item paths
     */
    /**
     * Data item root path for drone related events;
     */
    private static final String DRONE_EVENTS_PATH = "/drone/events";

    /**
     * Data item path for the drone info.
     */
    public static final String DRONE_INFO_PATH = DRONE_EVENTS_PATH + "/info";

    /**
     * Data item path for the drone connection state
     */
    public static final String DRONE_STATE_PATH = DRONE_EVENTS_PATH + "/drone_state";

    /*
    Data map keys
     */
    //State
    public static final String KEY_DRONE_CONNECTION_STATE = "key_drone_connection_state";
    public static final String KEY_DRONE_FOLLOW_STATE = "key_drone_follow_state";
    public static final String KEY_DRONE_FLIGHT_MODE = "key_drone_flight_mode";
    public static final String KEY_DRONE_TYPE = "key_drone_type";

    //Info
    public static final String KEY_DRONE_HOME = "key_drone_home";
    public static final String KEY_DRONE_GPS = "key_drone_gps";
    public static final String KEY_DRONE_BATTERY = "key_drone_battery";
    public static final String KEY_DRONE_FLIGHT_TIME = "key_drone_flight_time";
    public static final String KEY_DRONE_SIGNAL = "key_drone_signal";
    public static final String KEY_DRONE_ORIENTATION = "key_drone_orientation";
    public static final String KEY_DRONE_SPEED = "key_drone_speed";
    public static final String KEY_DRONE_ALTITUDE = "key_drone_altitude";

    /*
    Wear message paths constants and functions
     */
    /**
     * Root path for the drone related wear messages.
     */
    private static final String MESSAGE_ROOT_PATH = "/drone/actions";

    /**
     * Path for the message used to signal the main app was started.
     */
    public static final String MAIN_APP_STARTED_PATH = MESSAGE_ROOT_PATH + "/main_app_started";

    /**
     * Path for the message used to signal the main app was stopped.
     */
    public static final String MAIN_APP_STOPPED_PATH = MESSAGE_ROOT_PATH + "/main_app_stopped";

    /**
     * Path for the message used to signal that user interaction with the main app is required.
     */
    public static final String MAIN_APP_USE_REQUIRED_PATH = MESSAGE_ROOT_PATH +
            "/main_app_use_required";

    /**
     * Path for the message used to reset the drone flight time.
     */
    public static final String RESET_DRONE_FLIGHT_TIME_PATH = MESSAGE_ROOT_PATH +
            "/reset_flight_time";

    /**
     * Path for the message used to toggle connection with the drone.
     */
    public static final String TOGGLE_DRONE_CONNECTION_PATH = MESSAGE_ROOT_PATH +
            "/toggle_connection";

    /**
     * Path for the message used to toggle the drone follow-me mode.
     */
    public static final String TOGGLE_DRONE_FOLLOW_ME_PATH = MESSAGE_ROOT_PATH +
            "/toggle_follow_me";

    /**
     * Path for the message used to select the drone flight mode.
     */
    public static final String SET_DRONE_FLIGHT_MODE_PATH = MESSAGE_ROOT_PATH + "/set_flight_mode";

    /**
     * Encode the msg data to send with a 'TOGGLE_DRONE_FOLLOW_ME_PATH' message.
     *
     * @param shouldEnable true is follow-me mode should be enabled.
     * @return msg data byte array to send
     */
    public static byte[] encodeFollowMeMsgData(boolean shouldEnable) {
        byte[] encoded = {shouldEnable ? (byte) 1 : (byte) 0};
        return encoded;
    }

    /**
     * Decode the msg data received from a 'TOGGLE_DRONE_FOLLOW_ME_PATH' message.
     *
     * @param msgData received msg data
     * @return true if follow-me mode should be enabled.
     */
    public static boolean decodeFollowMeMsgData(byte[] msgData) {
        ensureMsgDataLength(msgData, 1);
        return msgData[0] == (byte) 1;
    }

    /**
     * Encode the msg data to send with a 'TOGGLE_DRONE_CONNECTION_PATH' message.
     *
     * @param shouldConnect true is drone connection should be established.
     * @return msg data byte array to send
     */
    public static byte[] encodeDroneConnectionMsgData(boolean shouldConnect) {
        byte[] encoded = {shouldConnect ? (byte) 1 : (byte) 0};
        return encoded;
    }

    /**
     * Decode the msg data received from a 'TOGGLE_DRONE_CONNECTION_PATH' message.
     *
     * @param msgData received msg data
     * @return true if drone connection should be enabled.
     */
    public static boolean decodeDroneConnectionMsgData(byte[] msgData) {
        ensureMsgDataLength(msgData, 1);
        return msgData[0] == (byte) 1;
    }

    /**
     * Encode the msg data to send with a 'SET_DRONE_FLIGHT_MODE_PATH' message.
     *
     * @param flightMode flight mode
     * @return msg data byte array to send
     */
    public static byte[] encodeFlightModeMsgData(ApmModes flightMode) {
        byte[] encoded = new byte[2];
        encoded[0] = (byte) flightMode.getNumber();
        encoded[1] = (byte) flightMode.getType();
        return encoded;
    }

    /**
     * Decode the msg data received from a 'SET_DRONE_FLIGHT_MODE_PATH' message.
     *
     * @param msgData received msg data
     * @return the decoded flight mode
     */
    public static ApmModes decodeFlightModeMsgData(byte[] msgData) {
        ensureMsgDataLength(msgData, 2);
        int modeNumber = msgData[0];
        int modeType = msgData[1];
        return ApmModes.getMode(modeNumber, modeType);
    }

    /**
     * Check the passed byte array for consistency.
     *
     * @param msgData        byte array to check
     * @param expectedLength expected length
     */
    private static void ensureMsgDataLength(byte[] msgData, int expectedLength) {
        if (msgData == null || msgData.length != expectedLength) {
            throw new IllegalArgumentException("Data to decode doesn't have the expected size.");
        }
    }

    /**
     * Asynchronously send a message using the Wearable.MessageApi api to connected wear nodes.
     *
     * @param apiClientMgr google api client manager
     * @param msgPath      non-null path for the message
     * @param msgData      optional message data
     * @return true if the message task was successfully queued.
     */
    public static boolean asyncSendMessage(GoogleApiClientManager apiClientMgr,
                                           final String msgPath, final byte[] msgData) {
        return apiClientMgr.addTaskToBackground(apiClientMgr.new GoogleApiClientTask() {

            @Override
            public void doRun() {
                final GoogleApiClient apiClient = getGoogleApiClient();

                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi
                        .getConnectedNodes(apiClient)
                        .await();

                for (Node node : nodes.getNodes()) {
                    final MessageApi.SendMessageResult result = Wearable.MessageApi
                            .sendMessage(apiClient, node.getId(), msgPath, msgData)
                            .await();

                    final Status status = result.getStatus();
                    if (!status.isSuccess()) {
                        Log.e(TAG, "Failed to relay the data: " + status.getStatusCode());
                    }
                }
            }
        });
    }

    /**
     * Asynchronously push/update a data item using the Wearable.DataApi api to connected wear
     * nodes.
     * @param apiClientMgr google api client manager
     * @param path non-null path
     * @param dataMapBundle non-null data bundle
     * @return true if the task was successfully queued.
     */
    public static boolean asyncPutDataItem(GoogleApiClientManager apiClientMgr,
                                           final String path, final Bundle dataMapBundle) {
        return apiClientMgr.addTaskToBackground(apiClientMgr.new GoogleApiClientTask() {

            @Override
            public void doRun() {
                final PutDataMapRequest dataMap = PutDataMapRequest.create(path);
                dataMap.getDataMap().putAll(DataMap.fromBundle(dataMapBundle));
                PutDataRequest request = dataMap.asPutDataRequest();
                final DataApi.DataItemResult result = Wearable.DataApi
                        .putDataItem(getGoogleApiClient(), request)
                        .await();

                final Status status = result.getStatus();
                if (!status.isSuccess()) {
                    Log.e(TAG, "Failed to relay the data: " + status.getStatusCode());
                }
            }
        });
    }

    /**
     * Asynchronously push/update a data item using the Wearable.DataApi api to connected wear
     * nodes.
     * @param apiClientMgr google api client manager
     * @param path non-null path
     * @param data non-null data payload
     * @return true if the task was successfully queued.
     */
    public static boolean asyncPutDataItem(GoogleApiClientManager apiClientMgr,
                                           final String path, final byte[] data) {
        return apiClientMgr.addTaskToBackground(apiClientMgr.new GoogleApiClientTask() {

            @Override
            public void doRun() {
                final PutDataRequest request = PutDataRequest.create(path);
                request.setData(data);
                final DataApi.DataItemResult result = Wearable.DataApi
                        .putDataItem(getGoogleApiClient(), request)
                        .await();

                final Status status = result.getStatus();
                if (!status.isSuccess()) {
                    Log.e(TAG, "Failed to relay the data: " + status.getStatusCode());
                }
            }
        });
    }
}
