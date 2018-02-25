package com.o3dr.services.android.lib.drone.action;

import com.o3dr.services.android.lib.util.Utils;

/**
 * Created by Fredia Huya-Kouadio on 9/7/15.
 */
public class ControlActions {

    private static final String PACKAGE_NAME = "com.o3dr.services.android.lib.drone.action.control";

    public static final String ACTION_DO_GUIDED_TAKEOFF = Utils.PACKAGE_NAME + ".action.DO_GUIDED_TAKEOFF";
    public static final String EXTRA_ALTITUDE = "extra_altitude";

    public static final String ACTION_SEND_GUIDED_POINT = Utils.PACKAGE_NAME + ".action.SEND_GUIDED_POINT";
    public static final String EXTRA_GUIDED_POINT = "extra_guided_point";

    public static final String EXTRA_FORCE_GUIDED_POINT = "extra_force_guided_point";
    public static final String ACTION_SET_GUIDED_ALTITUDE = Utils.PACKAGE_NAME + ".action.SET_GUIDED_ALTITUDE";

    public static final String ACTION_SET_CONDITION_YAW = PACKAGE_NAME + ".SET_CONDITION_YAW";
    public static final String EXTRA_YAW_TARGET_ANGLE = "extra_yaw_target_angle";
    public static final String EXTRA_YAW_CHANGE_RATE = "extra_yaw_change_rate";
    public static final String EXTRA_YAW_IS_RELATIVE = "extra_yaw_is_relative";

    public static final String ACTION_SET_VELOCITY = PACKAGE_NAME + ".SET_VELOCITY";

    public static final String ACTION_SEND_BRAKE_VEHICLE = PACKAGE_NAME + ".action.SEND_BRAKE_VEHICLE";

    /**
     * X velocity in meters per second.
     */
    public static final String EXTRA_VELOCITY_X = "extra_velocity_x";

    /**
     * Y velocity in meters per second.
     */
    public static final String EXTRA_VELOCITY_Y = "extra_velocity_y";

    /**
     * Z velocity in meters per second.
     */
    public static final String EXTRA_VELOCITY_Z = "extra_velocity_z";

    public static final String ACTION_ENABLE_MANUAL_CONTROL = PACKAGE_NAME + ".ENABLE_MANUAL_CONTROL";

    public static final String EXTRA_DO_ENABLE = "extra_do_enable";

    public static final String ACTION_LOOK_AT_TARGET = PACKAGE_NAME + ".action.LOOK_AT_TARGET";

    /**
     * Geo coordinate to orient the vehicle to
     */
    public static final String EXTRA_LOOK_AT_TARGET = "extra_look_at_target";

    //Private to prevent instantiation
    private ControlActions(){}


}
