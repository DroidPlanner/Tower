package com.o3dr.services.android.lib.drone.attribute;

/**
 * Stores all possible drone events.
 */
public class AttributeEvent {

    //Private to prevent instantiation
    private AttributeEvent() {
    }

    private static final String PACKAGE_NAME = "com.o3dr.services.android.lib.attribute.event";

    /**
     * Attitude attribute events.
     */
    public static final String ATTITUDE_UPDATED = PACKAGE_NAME + ".ATTITUDE_UPDATED";

    /**
     * Signals an autopilot error.
     *
     * @see {@link com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra#EXTRA_AUTOPILOT_ERROR_ID}
     */
    public static final String AUTOPILOT_ERROR = PACKAGE_NAME + ".AUTOPILOT_ERROR";

    /**
     * Event describing a message received from the autopilot.
     * The message content can be retrieved using the {@link com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra#EXTRA_AUTOPILOT_MESSAGE} key.
     * The message level can be retrieved using the {@link com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra#EXTRA_AUTOPILOT_MESSAGE_LEVEL} key.
     */
    public static final String AUTOPILOT_MESSAGE = PACKAGE_NAME + ".AUTOPILOT_MESSAGE";

    /**
     * Event to signal cancellation of the magnetometer calibration process.
     */
    public static final String CALIBRATION_MAG_CANCELLED = PACKAGE_NAME + ".CALIBRATION_MAG_CANCELLED";

    /**
     * Signals completion of the magnetometer calibration.
     *
     * @see {@link AttributeEventExtra#EXTRA_CALIBRATION_MAG_RESULT}
     */
    public static final String CALIBRATION_MAG_COMPLETED = PACKAGE_NAME + ".CALIBRATION_MAG_COMPLETED";

    /**
     * Provides progress updates for the magnetometer calibration.
     *
     * @see {@link AttributeEventExtra#EXTRA_CALIBRATION_MAG_PROGRESS}
     */
    public static final String CALIBRATION_MAG_PROGRESS = PACKAGE_NAME + ".CALIBRATION_MAG_PROGRESS";

    public static final String CALIBRATION_IMU = PACKAGE_NAME + ".CALIBRATION_IMU";
    public static final String CALIBRATION_IMU_TIMEOUT = PACKAGE_NAME + ".CALIBRATION_IMU_TIMEOUT";

    public static final String FOLLOW_START = PACKAGE_NAME + ".FOLLOW_START";
    public static final String FOLLOW_STOP = PACKAGE_NAME + ".FOLLOW_STOP";
    public static final String FOLLOW_UPDATE = PACKAGE_NAME + ".FOLLOW_UPDATE";

    /**
     * Camera attribute events.
     */
    public static final String CAMERA_UPDATED = PACKAGE_NAME + ".CAMERA_UPDATED";
    public static final String CAMERA_FOOTPRINTS_UPDATED = PACKAGE_NAME + ".CAMERA_FOOTPRINTS_UPDATED";

    /**
     * GuidedState attribute events.
     */
    public static final String GUIDED_POINT_UPDATED = PACKAGE_NAME + ".GUIDED_POINT_UPDATED";

    /**
     * Mission attribute events.
     */
    public static final String MISSION_UPDATED = PACKAGE_NAME + ".MISSION_UPDATED";
    public static final String MISSION_DRONIE_CREATED = PACKAGE_NAME + ".MISSION_DRONIE_CREATED";
    public static final String MISSION_SENT = PACKAGE_NAME + ".MISSION_SENT";
    public static final String MISSION_RECEIVED = PACKAGE_NAME + ".MISSION_RECEIVED";
    public static final String MISSION_ITEM_UPDATED = PACKAGE_NAME + ".MISSION_ITEM_UPDATED";
    public static final String MISSION_ITEM_REACHED = PACKAGE_NAME + ".MISSION_ITEM_REACHED";

    /*
     * Parameter attribute events.
     */

    /**
     * Event to signal the start of parameters refresh from the vehicle.
     *
     * @see {@link com.o3dr.services.android.lib.drone.property.Parameters}
     * @see {@link com.o3dr.services.android.lib.drone.property.Parameter}
     */
    public static final String PARAMETERS_REFRESH_STARTED = PACKAGE_NAME + ".PARAMETERS_REFRESH_STARTED";

    /**
     * Event to signal the completion of the parameters refresh from the vehicle.
     *
     * @see {@link com.o3dr.services.android.lib.drone.property.Parameters}
     * @see {@link com.o3dr.services.android.lib.drone.property.Parameter}
     */
    public static final String PARAMETERS_REFRESH_COMPLETED = PACKAGE_NAME + ".PARAMETERS_REFRESH_ENDED";

    /**
     * Event to signal receipt of a single parameter from the vehicle. During a parameters refresh, this event will
     * fire as many times as the count of the set of parameters being refreshed.
     * Allows listeners to keep track of the parameters refresh progress.
     *
     * @see {@link AttributeEventExtra#EXTRA_PARAMETER_INDEX}
     * @see {@link AttributeEventExtra#EXTRA_PARAMETERS_COUNT}
     * @see {@link AttributeEventExtra#EXTRA_PARAMETER_NAME}
     * @see {@link AttributeEventExtra#EXTRA_PARAMETER_VALUE}
     */
    public static final String PARAMETER_RECEIVED = PACKAGE_NAME + ".PARAMETERS_RECEIVED";

    /**
     * Event to signal update of the vehicle type.
     */
    public static final String TYPE_UPDATED = PACKAGE_NAME + ".TYPE_UPDATED";

    /**
     * Signal attribute events.
     */
    public static final String SIGNAL_UPDATED = PACKAGE_NAME + ".SIGNAL_UPDATED";
    public static final String SIGNAL_WEAK = PACKAGE_NAME + ".SIGNAL_WEAK";

    /**
     * Speed attribute events.
     */
    public static final String SPEED_UPDATED = PACKAGE_NAME + ".SPEED_UPDATED";

    /**
     * Battery attribute events.
     */
    public static final String BATTERY_UPDATED = PACKAGE_NAME + ".BATTERY_UPDATED";

    /*
     * State attribute events.
     */
    /**
     * Signals changes in the vehicle readiness (i.e: standby or active/airborne).
     */
    public static final String STATE_UPDATED = PACKAGE_NAME + ".STATE_UPDATED";

    /**
     * Signals changes in the vehicle arming state.
     */
    public static final String STATE_ARMING = PACKAGE_NAME + ".STATE_ARMING";
    public static final String STATE_CONNECTING = PACKAGE_NAME + ".STATE_CONNECTING";
    public static final String STATE_CONNECTED = PACKAGE_NAME + ".STATE_CONNECTED";
    public static final String STATE_DISCONNECTED = PACKAGE_NAME + ".STATE_DISCONNECTED";

    /**
     * Signals updates of the ekf status.
     * @see {@link com.o3dr.services.android.lib.drone.property.State}
     */
    public static final String STATE_EKF_REPORT = PACKAGE_NAME + ".STATE_EKF_REPORT";

    /**
     * Signals updates to the ekf position state.
     * @see {@link com.o3dr.services.android.lib.drone.property.State}
     */
    public static final String STATE_EKF_POSITION = PACKAGE_NAME + ".STATE_EKF_POSITION";

    /**
     * Signals update of the vehicle mode.
     * @see {@link com.o3dr.services.android.lib.drone.property.State}
     */
    public static final String STATE_VEHICLE_MODE = PACKAGE_NAME + ".STATE_VEHICLE_MODE";

    /**
     * Signals vehicle vibration updates.
     * @see {@link com.o3dr.services.android.lib.drone.property.State}
     */
    public static final String STATE_VEHICLE_VIBRATION = PACKAGE_NAME + ".STATE_VEHICLE_VIBRATION";

    /**
     * Signals vehicle UID updates.
     * @see {@link com.o3dr.services.android.lib.drone.property.State}
     */
    public static final String STATE_VEHICLE_UID = PACKAGE_NAME + ".STATE_VEHICLE_UID";

    /**
     * Home attribute events.
     */
    public static final String HOME_UPDATED = PACKAGE_NAME + ".HOME_UPDATED";

    /**
     * Gps' attribute events.
     */
    public static final String GPS_POSITION = PACKAGE_NAME + ".GPS_POSITION";
    public static final String GPS_FIX = PACKAGE_NAME + ".GPS_FIX";
    public static final String GPS_COUNT = PACKAGE_NAME + ".GPS_COUNT";
    public static final String WARNING_NO_GPS = PACKAGE_NAME + ".WARNING_NO_GPS";

    public static final String HEARTBEAT_FIRST = PACKAGE_NAME + ".HEARTBEAT_FIRST";
    public static final String HEARTBEAT_RESTORED = PACKAGE_NAME + ".HEARTBEAT_RESTORED";
    public static final String HEARTBEAT_TIMEOUT = PACKAGE_NAME + ".HEARTBEAT_TIMEOUT";

    /**
     * Altitude's attribute events.
     */
    public static final String ALTITUDE_UPDATED = PACKAGE_NAME + ".ALTITUDE_UPDATED";

    /**
     * Signals the gimbal orientation was updated.
     *
     * @see {@link AttributeEventExtra#EXTRA_GIMBAL_ORIENTATION_PITCH}
     * @see {@link AttributeEventExtra#EXTRA_GIMBAL_ORIENTATION_ROLL}
     * @see {@link AttributeEventExtra#EXTRA_GIMBAL_ORIENTATION_YAW}
     */
    public static final String GIMBAL_ORIENTATION_UPDATED = PACKAGE_NAME + ".GIMBAL_ORIENTATION_UPDATED";

    /**
     * Signals an update to the return to me state.
     * Retrieves the current state via {@link AttributeEventExtra#EXTRA_RETURN_TO_ME_STATE}
     */
    public static final String RETURN_TO_ME_STATE_UPDATE = PACKAGE_NAME + ".RETURN_TO_ME_STATE_UPDATE";

}
