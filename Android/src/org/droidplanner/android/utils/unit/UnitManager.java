package org.droidplanner.android.utils.unit;

/**
 * Created by fhuya on 11/11/14.
 */
public class UnitManager {

    private static UnitProvider unitProvider = new MetricUnitProvider();

    public static UnitProvider getUnitProvider(){
        return unitProvider;
    }
}
