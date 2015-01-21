package org.droidplanner.android.utils.unit;

import android.content.Context;

import com.o3dr.android.client.utils.unit.MetricUnitProvider;
import com.o3dr.android.client.utils.unit.UnitProvider;

import org.droidplanner.android.utils.unit.systems.ImperialUnitSystem;
import org.droidplanner.android.utils.unit.systems.MetricUnitSystem;
import org.droidplanner.android.utils.unit.systems.UnitSystem;

/**
 * Created by fhuya on 11/11/14.
 */
public class UnitManager {

    private static final UnitSystem unitSystem = new MetricUnitSystem();

    public static UnitSystem getUnitSystem(Context context){
        return unitSystem;
    }
}
