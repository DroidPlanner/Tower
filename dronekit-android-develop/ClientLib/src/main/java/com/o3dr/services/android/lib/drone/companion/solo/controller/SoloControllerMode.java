package com.o3dr.services.android.lib.drone.companion.solo.controller;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Defines the available controller modes.
 * Created by Fredia Huya-Kouadio on 7/13/15.
 */
public class SoloControllerMode {

    @IntDef({UNKNOWN_MODE, MODE_1, MODE_2})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ControllerMode {}

    /**
     * Unknown controller mode.
     */
    public static final int UNKNOWN_MODE = 0;

    /**
     * Controller mode 1:
     * - Left stick: pitch/yaw
     * - Right stick: throttle/roll
     */
    public static final int MODE_1 = 1;

    /**
     * Controller mode 2:
     * - Left stick: throttle/yaw
     * - right stick: pitch/roll
     */
    public static final int MODE_2 = 2;

    //Private to prevent instantiation.
    private SoloControllerMode(){}
}
