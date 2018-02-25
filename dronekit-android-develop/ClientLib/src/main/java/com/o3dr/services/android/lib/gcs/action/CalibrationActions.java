package com.o3dr.services.android.lib.gcs.action;

import com.o3dr.services.android.lib.util.Utils;

/**
 * Created by Fredia Huya-Kouadio on 1/19/15.
 */
public class CalibrationActions {

    //Private to prevent instantiation
    private CalibrationActions(){}

    public static final String ACTION_START_IMU_CALIBRATION = Utils.PACKAGE_NAME + ".action.START_IMU_CALIBRATION";
    public static final String ACTION_SEND_IMU_CALIBRATION_ACK = Utils.PACKAGE_NAME + ".action" +
            ".SEND_IMU_CALIBRATION_ACK";

    public static final String EXTRA_IMU_STEP = "extra_step";

    public static final String ACTION_START_MAGNETOMETER_CALIBRATION = Utils.PACKAGE_NAME + ".action" +
            ".START_MAGNETOMETER_CALIBRATION";
    public static final String ACTION_ACCEPT_MAGNETOMETER_CALIBRATION = Utils.PACKAGE_NAME + ".action" +
            ".ACCEPT_MAGNETOMETER_CALIBRATION";
    public static final String ACTION_CANCEL_MAGNETOMETER_CALIBRATION = Utils.PACKAGE_NAME + ".action" +
            ".CANCEL_MAGNETOMETER_CALIBRATION";

    public static final String EXTRA_RETRY_ON_FAILURE = "extra_retry_on_failure";
    public static final String EXTRA_SAVE_AUTOMATICALLY = "extra_save_automatically";
    public static final String EXTRA_START_DELAY = "extra_start_delay";

}
