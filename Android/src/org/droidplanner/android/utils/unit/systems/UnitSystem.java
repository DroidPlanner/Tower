package org.droidplanner.android.utils.unit.systems;

import org.droidplanner.android.utils.unit.providers.area.AreaUnitProvider;
import org.droidplanner.android.utils.unit.providers.length.LengthUnitProvider;

/**
 * Created by Fredia Huya-Kouadio on 1/20/15.
 */
public interface UnitSystem {

    public LengthUnitProvider getLengthUnitProvider();

    public AreaUnitProvider getAreaUnitProvider();

}
