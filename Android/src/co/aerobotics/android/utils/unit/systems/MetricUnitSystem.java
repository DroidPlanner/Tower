package co.aerobotics.android.utils.unit.systems;

import co.aerobotics.android.utils.unit.providers.area.AreaUnitProvider;
import co.aerobotics.android.utils.unit.providers.area.MetricAreaUnitProvider;
import co.aerobotics.android.utils.unit.providers.length.LengthUnitProvider;
import co.aerobotics.android.utils.unit.providers.length.MetricLengthUnitProvider;
import co.aerobotics.android.utils.unit.providers.speed.MetricSpeedUnitProvider;
import co.aerobotics.android.utils.unit.providers.speed.SpeedUnitProvider;

/**
 * Created by Fredia Huya-Kouadio on 1/20/15.
 */
public class MetricUnitSystem implements UnitSystem {

    private static final LengthUnitProvider lengthUnitProvider = new MetricLengthUnitProvider();
    private static final AreaUnitProvider areaUnitProvider = new MetricAreaUnitProvider();
    private static final SpeedUnitProvider speedUnitProvider = new MetricSpeedUnitProvider();

    @Override
    public LengthUnitProvider getLengthUnitProvider() {
        return lengthUnitProvider;
    }

    @Override
    public AreaUnitProvider getAreaUnitProvider() {
        return areaUnitProvider;
    }

    @Override
    public SpeedUnitProvider getSpeedUnitProvider() {
        return speedUnitProvider;
    }

    @Override
    public int getType() {
        return METRIC;
    }
}
