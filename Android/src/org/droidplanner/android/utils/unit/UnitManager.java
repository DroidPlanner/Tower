package org.droidplanner.android.utils.unit;

import com.o3dr.android.client.utils.unit.MetricUnitProvider;
import com.o3dr.android.client.utils.unit.UnitProvider;

/**
 * Created by fhuya on 11/11/14.
 */
public class UnitManager {

    private static UnitProvider unitProvider = new MetricUnitProvider();

    public static UnitProvider getUnitProvider(){
        return unitProvider;
    }
}
