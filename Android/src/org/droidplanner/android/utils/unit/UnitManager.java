package org.droidplanner.android.utils.unit;

import android.content.Context;

import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.android.utils.unit.systems.ImperialUnitSystem;
import org.droidplanner.android.utils.unit.systems.MetricUnitSystem;
import org.droidplanner.android.utils.unit.systems.UnitSystem;

import java.util.Locale;

/**
 * Created by fhuya on 11/11/14.
 */
public class UnitManager {

    private static DroidPlannerPrefs dpPrefs;
    private static MetricUnitSystem metricUnitSystem;
    private static ImperialUnitSystem imperialUnitSystem;

    public static UnitSystem getUnitSystem(Context context){
        if(dpPrefs == null)
            dpPrefs = DroidPlannerPrefs.getInstance(context);

        final int unitSystemType = dpPrefs.getUnitSystemType();
        switch(unitSystemType){
            case UnitSystem.AUTO:
            default:
                Locale locale = Locale.getDefault();
                if(Locale.US.equals(locale)) {
                    if(imperialUnitSystem == null)
                        imperialUnitSystem = new ImperialUnitSystem();
                    return imperialUnitSystem;
                }
                else {
                    if (metricUnitSystem == null)
                        metricUnitSystem = new MetricUnitSystem();
                    return metricUnitSystem;
                }

            case UnitSystem.METRIC:
                if(metricUnitSystem == null)
                    metricUnitSystem = new MetricUnitSystem();
                return metricUnitSystem;

            case UnitSystem.IMPERIAL:
                if(imperialUnitSystem == null)
                    imperialUnitSystem = new ImperialUnitSystem();
                return imperialUnitSystem;
        }
    }
}
