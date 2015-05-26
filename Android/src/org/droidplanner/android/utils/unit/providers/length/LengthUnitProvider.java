package org.droidplanner.android.utils.unit.providers.length;

import org.beyene.sius.operation.Operation;
import org.beyene.sius.unit.UnitIdentifier;
import org.beyene.sius.unit.impl.FactoryLength;
import org.beyene.sius.unit.length.LengthUnit;
import org.beyene.sius.unit.length.Meter;

/**
 * Created by Fredia Huya-Kouadio on 1/20/15.
 */
public abstract class LengthUnitProvider {

    public Meter boxBaseValue(double valueInMeters) {
        return FactoryLength.meter(valueInMeters);
    }

    public LengthUnit boxBaseValueToTarget(double valueInMeters) {
        Meter base = boxBaseValue(valueInMeters);
        return fromBaseToTarget(base);
    }

    public abstract LengthUnit fromBaseToTarget(Meter base);

    public Meter fromTargetToBase(LengthUnit target) {
        if(target instanceof Meter)
            return (Meter) target;

        return Operation.convert(target, UnitIdentifier.METER);
    }

    public Meter fromTargetToBase(double valueInTargetUnits){
        return fromTargetToBase(boxTargetValue(valueInTargetUnits));
    }

    public abstract LengthUnit boxTargetValue(double valueInTargetUnits);
}
