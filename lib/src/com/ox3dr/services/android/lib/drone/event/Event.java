package com.ox3dr.services.android.lib.drone.event;

/**
 * Stores all possible drone events.
 */
public class Event {

    private static final String CLAZZ_NAME = Event.class.getName();

    public static final String EVENT_CALIBRATION_MAG = CLAZZ_NAME + ".CALIBRATION_MAG";

    public static final String EVENT_CALIBRATION_IMU = CLAZZ_NAME + ".CALIBRATION_IMU";

    public static final String EVENT_CALIBRATION_TIMEOUT = CLAZZ_NAME + ".CALIBRATION_TIMEOUT";

    public static final String EVENT_CONNECTED = CLAZZ_NAME + ".CONNECTED";

    public static final String EVENT_DISCONNECTED = CLAZZ_NAME + ".DISCONNECTED";

    public static final String EVENT_PARAMETERS_REFRESH_STARTED = CLAZZ_NAME +
            ".PARAMETERS_REFRESH_STARTED";

    public static final String EVENT_PARAMETERS_REFRESH_ENDED = CLAZZ_NAME +
            ".PARAMETERS_REFRESH_ENDED";

    public static final String EVENT_PARAMETERS_RECEIVED = CLAZZ_NAME + ".PARAMETERS_RECEIVED";

    public static final String EVENT_TYPE_UPDATED = CLAZZ_NAME + ".TYPE_UPDATED";

    public static final String EVENT_HEARTBEAT_TIMEOUT = CLAZZ_NAME + ".HEARTBEAT_TIMEOUT";

    public static final String EVENT_VEHICLE_MODE = CLAZZ_NAME + ".VEHICLE_MODE";

    public static final String EVENT_RADIO = CLAZZ_NAME + ".RADIO";

    public static final String EVENT_ARMING = CLAZZ_NAME + ".ARMING";

    public static final String EVENT_AUTOPILOT_FAILSAFE = CLAZZ_NAME + ".AUTOPILOT_FAILSAFE";

    public static final String EVENT_ATTITUDE = CLAZZ_NAME + ".ATTITUDE";
    public static final String EVENT_SPEED = CLAZZ_NAME + ".SPEED";
    public static final String EVENT_BATTERY = CLAZZ_NAME + ".BATTERY";
    public static final String EVENT_STATE = CLAZZ_NAME + ".STATE";
    public static final String EVENT_HOME = CLAZZ_NAME + ".HOME";
    public static final String EVENT_GPS = CLAZZ_NAME + ".GPS";
    public static final String EVENT_HEARTBEAT_FIRST = CLAZZ_NAME + ".HEARTBEAT_FIRST";
    public static final String EVENT_HEARTBEAT_RESTORED = CLAZZ_NAME + ".HEARTBEAT_RESTORED";

}
