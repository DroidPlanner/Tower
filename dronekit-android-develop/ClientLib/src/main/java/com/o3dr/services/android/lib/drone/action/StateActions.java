package com.o3dr.services.android.lib.drone.action;

import com.o3dr.services.android.lib.util.Utils;

/**
 * Created by Fredia Huya-Kouadio on 1/19/15.
 */
public class StateActions {

    private static final String PACKAGE_NAME = "com.o3dr.services.android.lib.drone.action.state";

    public static final String ACTION_ARM = Utils.PACKAGE_NAME + ".action.ARM";
    public static final String EXTRA_ARM = "extra_arm";
    public static final String EXTRA_EMERGENCY_DISARM = "extra_emergency_disarm";

    public static final String ACTION_SET_VEHICLE_MODE = Utils.PACKAGE_NAME + ".action.SET_VEHICLE_MODE";
    public static final String EXTRA_VEHICLE_MODE = "extra_vehicle_mode";

    public static final String ACTION_SET_VEHICLE_HOME = Utils.PACKAGE_NAME + ".action.SET_VEHICLE_HOME";
    public static final String EXTRA_VEHICLE_HOME_LOCATION = "extra_vehicle_home_location";

    public static final String ACTION_ENABLE_RETURN_TO_ME = Utils.PACKAGE_NAME + ".action.ENABLE_RETURN_TO_ME";
    public static final String EXTRA_IS_RETURN_TO_ME_ENABLED = "extra_is_return_to_me_enabled";

    public static final String ACTION_UPDATE_VEHICLE_DATA_STREAM_RATE = PACKAGE_NAME + ".action.UPDATE_VEHICLE_DATA_STREAM_RATE";
    public static final String EXTRA_VEHICLE_DATA_STREAM_RATE = "extra_vehicle_data_stream_rate";

    //Private to prevent instantiation
    private StateActions(){}
}
