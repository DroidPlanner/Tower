package com.o3dr.services.android.lib.drone.action;

import com.o3dr.services.android.lib.util.Utils;

/**
 * Created by Fredia Huya-Kouadio on 1/19/15.
 */
public class ExperimentalActions {

    //Private to prevent instantiation
    private ExperimentalActions(){}

    public static final String ACTION_TRIGGER_CAMERA = Utils.PACKAGE_NAME + ".action.TRIGGER_CAMERA";

    public static final String ACTION_SET_ROI = Utils.PACKAGE_NAME + ".action.SET_ROI";
    public static final String EXTRA_SET_ROI_LAT_LONG_ALT = "extra_set_roi_lat_long_alt";

    public static final String ACTION_EPM_COMMAND = Utils.PACKAGE_NAME + ".action.EPM_COMMAND";
    public static final String EXTRA_EPM_RELEASE = Utils.PACKAGE_NAME + "extra_epm_release";

    public static final String ACTION_SEND_MAVLINK_MESSAGE = Utils.PACKAGE_NAME + ".action.SEND_MAVLINK_MESSAGE";
    public static final String EXTRA_MAVLINK_MESSAGE = "extra_mavlink_message";

    public static final String ACTION_SET_RELAY = Utils.PACKAGE_NAME + ".action.SET_RELAY";
    public static final String EXTRA_RELAY_NUMBER = "extra_relay_number";
    public static final String EXTRA_IS_RELAY_ON = "extra_is_relay_on";

    public static final String ACTION_SET_SERVO = Utils.PACKAGE_NAME + ".action.SET_SERVO";
    public static final String EXTRA_SERVO_CHANNEL = "extra_servo_channel";
    public static final String EXTRA_SERVO_PWM = "extra_servo_PWM";

    public static final String ACTION_START_VIDEO_STREAM_FOR_OBSERVER = Utils.PACKAGE_NAME + ".action.camera.START_VIDEO_STREAM_FOR_OBSERVER";
    public static final String ACTION_STOP_VIDEO_STREAM_FOR_OBSERVER = Utils.PACKAGE_NAME + ".action.camera.STOP_VIDEO_STREAM_FOR_OBSERVER";
}
