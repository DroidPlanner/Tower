package org.droidplanner.android.utils.unit.providers.area;

import org.beyene.sius.operation.Operation;
import org.beyene.sius.unit.UnitIdentifier;
import org.beyene.sius.unit.composition.area.AreaUnit;
import org.beyene.sius.unit.composition.area.Constants;
import org.beyene.sius.unit.composition.area.SquareMeter;

/**
 * Created by Fredia Huya-Kouadio on 1/21/15.
 */
public class MetricAreaUnitProvider extends AreaUnitProvider {
    @Override
    public AreaUnit fromBaseToTarget(SquareMeter base) {
        double absBase = Math.abs(base.getValue());
        if (absBase >= Constants.SQM_PER_SQKM)
            return Operation.convert(base, UnitIdentifier.SQUARE_KILOMETER);
        else if (absBase >= 0.1)
            return base;
        else
            return Operation.convert(base, UnitIdentifier.SQUARE_MILLIMETER);
    }
}
