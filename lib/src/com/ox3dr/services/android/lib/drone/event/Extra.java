package com.ox3dr.services.android.lib.drone.event;

/**
 * Holds handles used to retrieve additional information broadcast along a drone event.
 */
public class Extra {

    private static final String CLAZZ_NAME = Extra.class.getName();

    public static final String EXTRA_CALIBRATION_IMU_MESSAGE = CLAZZ_NAME +
            ".CALIBRATION_IMU_MESSAGE";

    /**
     * Used to access the mavlink version when the heartbeat is received for the first time or
     * restored.
     */
    public static final String EXTRA_MAVLINK_VERSION = CLAZZ_NAME + ".MAVLINK_VERSION";

    public static final String EXTRA_PARAMETERS_COUNT = CLAZZ_NAME + ".PARAMETERS_COUNT";
    public static final String EXTRA_PARAMETER_INDEX = CLAZZ_NAME + ".PARAMETER_INDEX";
}
