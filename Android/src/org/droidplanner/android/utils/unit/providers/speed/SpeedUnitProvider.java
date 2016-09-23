package org.droidplanner.android.utils.unit.providers.speed;

import org.beyene.sius.operation.Operation;
import org.beyene.sius.unit.UnitIdentifier;
import org.beyene.sius.unit.composition.speed.MeterPerSecond;
import org.beyene.sius.unit.composition.speed.SpeedUnit;
import org.beyene.sius.unit.impl.FactorySpeed;

/**
 * Created by Fredia Huya-Kouadio on 1/21/15.
 */
public abstract class SpeedUnitProvider {

    public MeterPerSecond boxBaseValue(double valueInMps){
        return FactorySpeed.mps(valueInMps);
    }

    public SpeedUnit boxBaseValueToTarget(double valueInMps){
        MeterPerSecond base = boxBaseValue(valueInMps);
        return fromBaseToTarget(base);
    }

    public abstract SpeedUnit fromBaseToTarget(MeterPerSecond base);

    public MeterPerSecond fromTargetToBase(SpeedUnit target){
        if(target instanceof MeterPerSecond)
            return (MeterPerSecond) target;

        return Operation.convert(target, UnitIdentifier.METER_PER_SECOND);
    }

    public abstract SpeedUnit boxTargetValue(double speedInTargetUnits);
}
