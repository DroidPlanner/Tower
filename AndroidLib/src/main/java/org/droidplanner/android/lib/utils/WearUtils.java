package org.droidplanner.android.lib.utils;

import com.MAVLink.Messages.ApmModes;

/**
 * Contains functions and constants related to the wear data item api.
 */
public class WearUtils {

    //Not instantiable
    private WearUtils(){}

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

    /*
    Data map keys
     */
    public static final String KEY_DRONE_FOLLOW_STATE = "key_drone_follow_state";
    public static final String KEY_DRONE_CONNECTION_STATE = "key_drone_connection_state";
    public static final String KEY_DRONE_FLIGHT_MODE = "key_drone_flight_mode";
    public static final String KEY_DRONE_TYPE = "key_drone_type";

    public static final String KEY_DRONE_HOME = "key_drone_home";
    public static final String KEY_DRONE_GPS = "key_drone_gps";
    public static final String KEY_DRONE_BATTERY = "key_drone_battery";
    public static final String KEY_DRONE_FLIGHT_TIME = "key_drone_flight_time";
    public static final String KEY_DRONE_SIGNAL = "key_drone_signal";

    public static final String KEY_DRONE_ROLL = "key_drone_roll";
    public static final String KEY_DRONE_YAW = "key_drone_yaw";
    public static final String KEY_DRONE_PITCH = "key_drone_pitch";
    public static final String KEY_DRONE_GROUND_SPEED = "key_drone_ground_speed";
    public static final String KEY_DRONE_AIR_SPEED = "key_drone_air_speed";
    public static final String KEY_DRONE_CLIMB_RATE = "key_drone_climb_rate";
    public static final String KEY_DRONE_ALTITUDE = "key_drone_altitude";

    /*
    Wear message paths constants and functions
     */
    /**
     * Root path for the drone related wear messages.
     */
    private static final String MESSAGE_ROOT_PATH = "/drone/actions";

    public static final String PHONE_USE_REQUIRED_PATH = MESSAGE_ROOT_PATH + "/phone_use_required";

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
     * @param shouldConnect true is follow-me mode should be enabled.
     * @return msg data byte array to send
     */
    public static byte[] encodeFollowMeMsgData(boolean shouldConnect){
        byte[] encoded = {shouldConnect ? (byte)1: (byte) 0};
        return encoded;
    }

    /**
     * Decode the msg data received from a 'TOGGLE_DRONE_FOLLOW_ME_PATH' message.
     * @param msgData received msg data
     * @return true if follow-me mode should be enabled.
     */
    public static boolean decodeFollowMeMsgData(byte[] msgData){
        ensureMsgDataLength(msgData, 1);
        return msgData[0] == (byte) 1;
    }

    /**
     * Encode the msg data to send with a 'TOGGLE_DRONE_CONNECTION_PATH' message.
     * @param shouldConnect true is drone connection should be established.
     * @return msg data byte array to send
     */
    public static byte[] encodeDroneConnectionMsgData(boolean shouldConnect){
        byte[] encoded = {shouldConnect ? (byte)1: (byte) 0};
        return encoded;
    }

    /**
     * Decode the msg data received from a 'TOGGLE_DRONE_CONNECTION_PATH' message.
     * @param msgData received msg data
     * @return true if drone connection should be enabled.
     */
    public static boolean decodeDroneConnectionMsgData(byte[] msgData){
        ensureMsgDataLength(msgData, 1);
        return msgData[0] == (byte) 1;
    }

    /**
     * Encode the msg data to send with a 'SET_DRONE_FLIGHT_MODE_PATH' message.
     * @param flightMode flight mode
     * @return msg data byte array to send
     */
    public static byte[] encodeFlightModeMsgData(ApmModes flightMode){
        byte[] encoded = new byte[2];
        encoded[0] = (byte) flightMode.getNumber();
        encoded[1] = (byte) flightMode.getType();
        return encoded;
    }

    /**
     * Decode the msg data received from a 'SET_DRONE_FLIGHT_MODE_PATH' message.
     * @param msgData received msg data
     * @return the decoded flight mode
     */
    public static ApmModes decodeFlightModeMsgData(byte[] msgData){
        ensureMsgDataLength(msgData, 2);
        int modeNumber = msgData[0];
        int modeType = msgData[1];
        return ApmModes.getMode(modeNumber, modeType);
    }

    /**
     * Check the passed byte array for consistency.
     * @param msgData byte array to check
     * @param expectedLength expected length
     */
    private static void ensureMsgDataLength(byte[] msgData, int expectedLength){
        if(msgData == null || msgData.length != expectedLength){
            throw new IllegalArgumentException("Data to decode doesn't have the expected size.");
        }
    }
}
