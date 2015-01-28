package org.droidplanner.android.utils.unit.providers.area;

import org.beyene.sius.operation.Operation;
import org.beyene.sius.unit.UnitIdentifier;
import org.beyene.sius.unit.composition.area.AreaUnit;
import org.beyene.sius.unit.composition.area.Constants;
import org.beyene.sius.unit.composition.area.SquareMeter;

/**
 * Created by Fredia Huya-Kouadio on 1/21/15.
 */
public class ImperialAreaUnitProvider extends AreaUnitProvider {
    @Override
    public AreaUnit fromBaseToTarget(SquareMeter base) {
        double absBase = Math.abs(base.getValue());
        if (absBase >= Constants.SQM_PER_SQMILE)
            return Operation.convert(base, UnitIdentifier.SQUARE_MILE);
        else if (absBase >= 0.1)
            return Operation.convert(base, UnitIdentifier.SQUARE_FOOT);
        else
            return Operation.convert(base, UnitIdentifier.SQUARE_INCH);
    }
}
