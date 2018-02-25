package com.o3dr.services.android.lib.drone.action;

import com.o3dr.services.android.lib.util.Utils;

/**
 * Created by Fredia Huya-Kouadio on 7/15/15.
 */
public class CapabilityActions {

    //Private to prevent instantiation
    private CapabilityActions(){}

    public static final String ACTION_CHECK_FEATURE_SUPPORT = Utils.PACKAGE_NAME + ".action.CHECK_FEATURE_SUPPORT";

    /**
     * Id of the feature whose support to check.
     */
    public static final String EXTRA_FEATURE_ID = "extra_feature_id";
}
