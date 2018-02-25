package com.o3dr.services.android.lib.drone.action;

import com.o3dr.services.android.lib.util.Utils;

public class GimbalActions {

    //Private to prevent instantiation
    private GimbalActions(){}

    public static final String GIMBAL_PITCH = "gimbal_pitch";
    public static final String GIMBAL_YAW = "gimbal_yaw";
    public static final String GIMBAL_ROLL = "gimbal_roll";

    public static final String ACTION_SET_GIMBAL_ORIENTATION = Utils.PACKAGE_NAME + ".action.gimbal" +
            ".SET_GIMBAL_ORIENTATION";

    public static final String ACTION_SET_GIMBAL_MOUNT_MODE = Utils.PACKAGE_NAME + ".action.gimbal.SET_GIMBAL_MOUNT_MODE";

    /**
     * Gimbal mount mode.
     * @see {@link com.MAVLink.enums.MAV_MOUNT_MODE}
     */
    public static final String GIMBAL_MOUNT_MODE ="gimbal_mount_mode";

    public static final String ACTION_RESET_GIMBAL_MOUNT_MODE = Utils.PACKAGE_NAME + ".action.gimbal.RESET_GIMBAL_MOUNT_MODE";

}
