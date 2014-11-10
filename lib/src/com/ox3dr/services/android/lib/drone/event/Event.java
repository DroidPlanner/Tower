package com.ox3dr.services.android.lib.drone.event;

/**
 * Stores all possible drone events.
 */
public class Event {

    private static final String CLAZZ_NAME = Event.class.getName();

    public static final String EVENT_ARMING = CLAZZ_NAME + ".ARMING";

    public static final String EVENT_ATTITUDE = CLAZZ_NAME + ".ATTITUDE";

    public static final String EVENT_AUTOPILOT_FAILSAFE = CLAZZ_NAME + ".AUTOPILOT_FAILSAFE";

    /**
     * Signals the start of magnetometer calibration.
     */
    public static final String EVENT_CALIBRATION_MAG_STARTED = CLAZZ_NAME + "" +
            ".CALIBRATION_MAG_STARTED";

    /**
     * Signals a magnetometer calibration fitness update.
     */
    public static final String EVENT_CALIBRATION_MAG_ESTIMATION = CLAZZ_NAME + "" +
            ".CALIBRATION_MAG_ESTIMATION";

    /**
     * Signals completion of the magnetometer calibration.
     */
    public static final String EVENT_CALIBRATION_MAG_COMPLETED = CLAZZ_NAME + "" +
            ".CALIBRATION_MAG_COMPLETED";

    public static final String EVENT_CALIBRATION_IMU = CLAZZ_NAME + ".CALIBRATION_IMU";

    public static final String EVENT_CALIBRATION_IMU_ERROR = CLAZZ_NAME + ".CALIBRATION_IMU_ERROR";

    public static final String EVENT_CALIBRATION_IMU_TIMEOUT = CLAZZ_NAME + "" +
            ".CALIBRATION_IMU_TIMEOUT";

    public static final String EVENT_CONNECTED = CLAZZ_NAME + ".CONNECTED";

    public static final String EVENT_DISCONNECTED = CLAZZ_NAME + ".DISCONNECTED";

    public static final String EVENT_FOLLOW_START = CLAZZ_NAME + ".FOLLOW_START";

    public static final String EVENT_FOLLOW_STOP = CLAZZ_NAME + ".FOLLOW_STOP";

    public static final String EVENT_FOLLOW_UPDATE = CLAZZ_NAME + ".FOLLOW_UPDATE";

    public static final String EVENT_GUIDED_POINT = CLAZZ_NAME + ".GUIDED_POINT";

    public static final String EVENT_MISSION_UPDATE = CLAZZ_NAME + ".MISSION_UPDATE";

    public static final String EVENT_MISSION_DRONIE_CREATED = CLAZZ_NAME + "" +
            ".MISSION_DRONIE_CREATED";

    public static final String EVENT_MISSION_SENT = CLAZZ_NAME + ".MISSION_SENT";
    public static final String EVENT_MISSION_RECEIVED = CLAZZ_NAME + ".MISSION_RECEIVED";

    public static final String EVENT_MISSION_ITEM_UPDATE = CLAZZ_NAME + ".MISSION_ITEM_UPDATE";

    public static final String EVENT_PARAMETERS_REFRESH_STARTED = CLAZZ_NAME +
            ".PARAMETERS_REFRESH_STARTED";

    public static final String EVENT_PARAMETERS_REFRESH_ENDED = CLAZZ_NAME +
            ".PARAMETERS_REFRESH_ENDED";

    public static final String EVENT_PARAMETERS_RECEIVED = CLAZZ_NAME + ".PARAMETERS_RECEIVED";

    public static final String EVENT_TYPE_UPDATED = CLAZZ_NAME + ".TYPE_UPDATED";

    public static final String EVENT_HEARTBEAT_TIMEOUT = CLAZZ_NAME + ".HEARTBEAT_TIMEOUT";

    public static final String EVENT_VEHICLE_MODE = CLAZZ_NAME + ".VEHICLE_MODE";

    public static final String EVENT_RADIO = CLAZZ_NAME + ".RADIO";

    public static final String EVENT_SPEED = CLAZZ_NAME + ".SPEED";
    public static final String EVENT_BATTERY = CLAZZ_NAME + ".BATTERY";
    public static final String EVENT_STATE = CLAZZ_NAME + ".STATE";
    public static final String EVENT_HOME = CLAZZ_NAME + ".HOME";
    public static final String EVENT_GPS = CLAZZ_NAME + ".GPS";
    public static final String EVENT_GPS_STATE = CLAZZ_NAME + ".GPS_STATE";
    public static final String EVENT_HEARTBEAT_FIRST = CLAZZ_NAME + ".HEARTBEAT_FIRST";
    public static final String EVENT_HEARTBEAT_RESTORED = CLAZZ_NAME + ".HEARTBEAT_RESTORED";

    public static final String EVENT_WARNING_400FT_EXCEEDED = CLAZZ_NAME + "" +
            ".WARNING_400FT_EXCEEDED";
    public static final String EVENT_WARNING_SIGNAL_WEAK = CLAZZ_NAME + ".WARNING_SIGNAL_WEAK";

    public static final String EVENT_WARNING_NO_GPS = CLAZZ_NAME + ".WARNING_NO_GPS";

}
