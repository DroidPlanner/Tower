package org.droidplanner.android.utils.unit.providers.area;

import org.beyene.sius.operation.Operation;
import org.beyene.sius.unit.UnitIdentifier;
import org.beyene.sius.unit.composition.area.AreaUnit;
import org.beyene.sius.unit.composition.area.SquareMeter;
import org.beyene.sius.unit.impl.FactoryArea;

/**
 * Created by Fredia Huya-Kouadio on 1/21/15.
 */
public abstract class AreaUnitProvider {

    public SquareMeter boxBaseValue(double valueInSqMeters){
        return FactoryArea.squareMeter(valueInSqMeters);
    }

    public AreaUnit boxBaseValueToTarget(double valueInSqMeters){
        SquareMeter base = boxBaseValue(valueInSqMeters);
        return fromBaseToTarget(base);
    }

    public abstract AreaUnit fromBaseToTarget(SquareMeter base);

    public SquareMeter fromTargetToBase(AreaUnit target){
        if(target instanceof SquareMeter)
            return (SquareMeter) target;

        return Operation.convert(target, UnitIdentifier.SQUARE_METER);
    }
}
