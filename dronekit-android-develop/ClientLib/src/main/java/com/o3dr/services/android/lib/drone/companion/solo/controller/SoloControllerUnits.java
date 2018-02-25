package com.o3dr.services.android.lib.drone.companion.solo.controller;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Defines the controller's supported unit systems.
 * Created by Fredia Huya-Kouadio on 9/7/15.
 */
public class SoloControllerUnits {

    @StringDef({UNKNOWN, METRIC, IMPERIAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ControllerUnit {
    }

    /**
     * Unknown controller unit
     */
    public static final String UNKNOWN = "unknown";

    /**
     * Metric unit system
     */
    public static final String METRIC = "metric";

    /**
     * Imperial unit system
     */
    public static final String IMPERIAL = "imperial";

    private SoloControllerUnits(){}
}
