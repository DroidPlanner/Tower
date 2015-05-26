package org.droidplanner.android.utils.unit.providers.length;

import org.beyene.sius.operation.Operation;
import org.beyene.sius.unit.UnitIdentifier;
import org.beyene.sius.unit.impl.FactoryLength;
import org.beyene.sius.unit.length.Constants;
import org.beyene.sius.unit.length.LengthUnit;
import org.beyene.sius.unit.length.Meter;

/**
 * Created by Fredia Huya-Kouadio on 1/20/15.
 */
public class MetricLengthUnitProvider extends LengthUnitProvider {

    @Override
    public LengthUnit fromBaseToTarget(Meter base) {
        double absBase = Math.abs(base.getValue());
        if(absBase >= Constants.METER_PER_KILOMETER)
            return Operation.convert(base, UnitIdentifier.KILOMETER);
        else
            return base;
    }

    @Override
    public LengthUnit boxTargetValue(double valueInTargetUnits) {
        return FactoryLength.meter(valueInTargetUnits);
    }
}
