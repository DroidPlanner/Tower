package com.o3dr.services.android.lib.drone.action;

import com.o3dr.services.android.lib.util.Utils;

/**
 * Contains builder methods use to generate connect or disconnect actions.
 */
public class ConnectionActions {

    //Private to prevent instantiation
    private ConnectionActions(){}

    public static final String ACTION_CONNECT = Utils.PACKAGE_NAME + ".action.CONNECT";
    public static final String EXTRA_CONNECT_PARAMETER = "extra_connect_parameter";

    public static final String ACTION_DISCONNECT = Utils.PACKAGE_NAME + ".action.DISCONNECT";
}
