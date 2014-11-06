package com.ox3dr.services.android.lib.drone.event;

/**
 * Holds handles used to retrieve additional information broadcast along a drone event.
 */
public class Extra {

    private static final String CLAZZ_NAME = Extra.class.getName();

    public static final String EXTRA_CALIBRATION_IMU_MESSAGE = CLAZZ_NAME +
            ".CALIBRATION_IMU_MESSAGE";

    /**
     * Used to access the points used to start the magnetometer calibration.
     */
    public static final String EXTRA_CALIBRATION_MAG_POINTS = CLAZZ_NAME + ".CALIBRATION_MAG_POINTS";

    /**
     * Used to access the magnetometer calibration fitness value.
     */
    public static final String EXTRA_CALIBRATION_MAG_FITNESS = CLAZZ_NAME + "" +
            ".CALIBRATION_MAG_FITNESS";

    /**
     * Used to access the magnetometer calibration fit center values.
     */
    public static final String EXTRA_CALIBRATION_MAG_FIT_CENTER = CLAZZ_NAME + "" +
            ".CALIBRATION_MAG_FIT_CENTER";

    /**
     * Used to access the magnetometer calibration fit radii values.
     */
    public static final String EXTRA_CALIBRATION_MAG_FIT_RADII = CLAZZ_NAME + "" +
            ".CALIBRATION_MAG_FIT_RADII";

    /**
     * Used to access the magnetometer calibration final offset values.
     */
    public static final String EXTRA_CALIBRATION_MAG_OFFSETS = CLAZZ_NAME + "" +
            ".CALIBRATION_MAG_OFFSETS";

    /**
     * Used to access the mavlink version when the heartbeat is received for the first time or
     * restored.
     */
    public static final String EXTRA_MAVLINK_VERSION = CLAZZ_NAME + ".MAVLINK_VERSION";

    public static final String EXTRA_MISSION_DRONIE_BEARING = CLAZZ_NAME + "" +
            ".MISSION_DRONIE_BEARING";

    public static final String EXTRA_PARAMETERS_COUNT = CLAZZ_NAME + ".PARAMETERS_COUNT";
    public static final String EXTRA_PARAMETER_INDEX = CLAZZ_NAME + ".PARAMETER_INDEX";
}
