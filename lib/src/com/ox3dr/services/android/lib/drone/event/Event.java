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


}
