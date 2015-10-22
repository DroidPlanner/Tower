package org.droidplanner.android.view.spinnerWheel.adapters;

import android.content.Context;
import android.text.TextUtils;

import org.beyene.sius.operation.Operation;
import org.beyene.sius.unit.composition.speed.SpeedUnit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fredia Huya-Kouadio on 1/21/15.
 */
public class SpeedWheelAdapter extends AbstractWheelTextAdapter<SpeedUnit>{

    private final List<SpeedUnit> unitsList = new ArrayList<>();

    public SpeedWheelAdapter(Context context, int itemResource, SpeedUnit startSpeed, SpeedUnit endSpeed){
        super(context, itemResource);
        generateUnits(startSpeed, endSpeed);
    }

    private void generateUnits(SpeedUnit startSpeed, SpeedUnit endSpeed){
        if(!startSpeed.getClass().equals(endSpeed.getClass())){
            endSpeed = (SpeedUnit) Operation.convert(endSpeed, startSpeed.getIdentifier());
        }

        final int startValue = (int) Math.round(startSpeed.getValue());
        final int endValue = (int) Math.round(endSpeed.getValue());

        if(startValue > endValue)
            throw new IllegalArgumentException("Starting value must be less or equal to the ending value.");

        unitsList.clear();
        for(double i = startValue; i <= endValue; i+=0.5){
            unitsList.add((SpeedUnit) startSpeed.valueOf(i));
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
    public SpeedUnit getItem(int index) {
        return unitsList.get(index);
    }

    @Override
    public int getItemIndex(SpeedUnit item) {
        final double roundedValue = roundToClosestUnit(item.getValue());
        final SpeedUnit floorItem = (SpeedUnit) item.valueOf(roundedValue);
        return unitsList.indexOf(floorItem);
    }

    private double roundToClosestUnit(double value){
        final int valuex10 = (int) (value * 10);
        final int divider = valuex10 / 5;
        final double roundedValue = (divider * 5) / 10.0;
        return roundedValue;
    }

    @Override
    public SpeedUnit parseItemText(CharSequence itemText) {
        String text = itemText.toString();
        if(TextUtils.isEmpty(text))
            return (SpeedUnit) unitsList.get(0).valueOf(0);

        return (SpeedUnit) unitsList.get(0).valueOf(Double.parseDouble(text));
    }
}
