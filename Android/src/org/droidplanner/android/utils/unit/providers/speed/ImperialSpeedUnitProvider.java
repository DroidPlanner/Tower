package org.droidplanner.android.utils.unit.providers.speed;

import org.beyene.sius.operation.Operation;
import org.beyene.sius.unit.UnitIdentifier;
import org.beyene.sius.unit.composition.speed.MeterPerSecond;
import org.beyene.sius.unit.composition.speed.SpeedUnit;

/**
 * Created by Fredia Huya-Kouadio on 1/21/15.
 */
public class ImperialSpeedUnitProvider extends SpeedUnitProvider {
    @Override
    public SpeedUnit fromBaseToTarget(MeterPerSecond base) {
        return Operation.convert(base, UnitIdentifier.MILES_PER_HOUR);
    }
}
