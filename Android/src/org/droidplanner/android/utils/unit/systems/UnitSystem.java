package org.droidplanner.android.utils.unit.systems;

import org.droidplanner.android.utils.unit.providers.area.AreaUnitProvider;
import org.droidplanner.android.utils.unit.providers.length.LengthUnitProvider;
import org.droidplanner.android.utils.unit.providers.speed.SpeedUnitProvider;

/**
 * Created by Fredia Huya-Kouadio on 1/20/15.
 */
public interface UnitSystem {

    public static final int AUTO = 0;
    public static final int METRIC = 1;
    public static final int IMPERIAL = 2;

    public LengthUnitProvider getLengthUnitProvider();

    public AreaUnitProvider getAreaUnitProvider();

    public SpeedUnitProvider getSpeedUnitProvider();

}
