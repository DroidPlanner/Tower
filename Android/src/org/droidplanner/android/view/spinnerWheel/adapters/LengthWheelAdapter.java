package org.droidplanner.android.view.spinnerWheel.adapters;

import android.content.Context;
import android.text.TextUtils;

import org.beyene.sius.operation.Operation;
import org.beyene.sius.unit.length.LengthUnit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fredia Huya-Kouadio on 1/21/15.
 */
public class LengthWheelAdapter extends AbstractWheelTextAdapter<LengthUnit> {

    private final List<LengthUnit> unitsList = new ArrayList<>();

    public LengthWheelAdapter(Context context, int itemResource, LengthUnit startUnit, LengthUnit endUnit) {
        super(context, itemResource);
        generateUnits(startUnit, endUnit);
    }

    private void generateUnits(LengthUnit startUnit, LengthUnit endUnit) {
        if (!startUnit.getClass().equals(endUnit.getClass())) {
            endUnit = (LengthUnit) Operation.convert(endUnit, startUnit.getIdentifier());
        }

        final int startValue = (int) Math.round(startUnit.getValue());
        final int endValue = (int) Math.round(endUnit.getValue());

        if (startValue > endValue)
            throw new IllegalArgumentException("Starting value must be less or equal to the ending value");

        unitsList.clear();
        for (int i = startValue; i <= endValue; i++) {
            unitsList.add((LengthUnit) startUnit.valueOf(i));
        }
    }

    @Override
    protected CharSequence getItemText(int index) {
        return unitsList.get(index).toString();
    }

    @Override
    public int getItemsCount() {
        return unitsList.size();
    }

    @Override
    public LengthUnit getItem(int index) {
        return unitsList.get(index);
    }

    @Override
    public int getItemIndex(LengthUnit item) {
        LengthUnit roundedItem = (LengthUnit) item.valueOf(Math.round(item.getValue()));
        return unitsList.indexOf(roundedItem);
    }

    @Override
    public LengthUnit parseItemText(CharSequence itemText) {
        String text = itemText.toString();
        if(TextUtils.isEmpty(text))
            return (LengthUnit) unitsList.get(0).valueOf(0);

        return (LengthUnit) unitsList.get(0).valueOf(Double.parseDouble(text));
    }
}
