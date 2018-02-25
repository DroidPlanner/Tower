package com.o3dr.services.android.lib.gcs.action;

import com.o3dr.services.android.lib.util.Utils;

/**
 * Created by Fredia Huya-Kouadio on 1/19/15.
 */
public class FollowMeActions {

    //Private to prevent instantiation
    private FollowMeActions(){}

    public static final String ACTION_ENABLE_FOLLOW_ME = Utils.PACKAGE_NAME + ".action.ENABLE_FOLLOW_ME";
    public static final String EXTRA_FOLLOW_TYPE = "extra_follow_type";

    public static final String ACTION_UPDATE_FOLLOW_PARAMS = Utils.PACKAGE_NAME + ".action.UPDATE_FOLLOW_PARAMS";

    public static final String ACTION_DISABLE_FOLLOW_ME = Utils.PACKAGE_NAME + ".action.DISABLE_FOLLOW_ME";

    public static final String ACTION_NEW_EXTERNAL_LOCATION = Utils.PACKAGE_NAME + ".action.NEW_EXTERNAL_LOCATION";
    public static final String EXTRA_LOCATION = "extra_location";
    public static final String EXTRA_LOCATION_SOURCE = "extra_location_source";
}
