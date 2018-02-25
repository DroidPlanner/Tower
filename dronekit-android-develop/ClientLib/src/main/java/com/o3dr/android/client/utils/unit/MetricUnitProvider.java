package com.o3dr.android.client.utils.unit;

import java.util.Locale;

/**
 * Unit provider implementation for the metric system.
 */
public class MetricUnitProvider implements UnitProvider {

    @Override
    public String areaToString(double areaInSqMeters) {
        double absArea = Math.abs(areaInSqMeters);

        if (absArea >= 100000) {
            return String.format(Locale.US, "%2.1f km" + SQUARE_SYMBOL, areaInSqMeters / 1000000);
        } else if (absArea >= 1) {
            return String.format(Locale.US, "%2.1f m" + SQUARE_SYMBOL, areaInSqMeters);
        } else if (absArea >= 0.00001) {
            return String.format(Locale.US, "%2.2f cm" + SQUARE_SYMBOL, areaInSqMeters * 10000);
        } else {
            return areaInSqMeters + " m" + SQUARE_SYMBOL;
        }
    }

    @Override
    public String distanceToString(double distanceInMeters) {
        double absDistance = Math.abs(distanceInMeters);

        if (absDistance >= 1000) {
            return String.format(Locale.US, "%2.1f km", distanceInMeters / 1000);
        } else if (absDistance >= 0.1) {
            return String.format(Locale.US, "%2.1f m", distanceInMeters);
        } else if (absDistance >= 0.001) {
            return String.format(Locale.US, "%2.1f mm", distanceInMeters * 1000);
        } else {
            return distanceInMeters + " m";
        }
    }

    @Override
    public String speedToString(double speedInMetersPerSeconds) {
        return String.format(Locale.US, "%2.1f m/s", speedInMetersPerSeconds);
    }

    @Override
    public String electricChargeToString(double chargeInmAh) {
        double absCharge = Math.abs(chargeInmAh);
        if(absCharge >= 1000){
            return String.format(Locale.US, "%2.0f Ah", chargeInmAh / 1000);
        }
        else{
            return String.format(Locale.ENGLISH, "%2.0f mAh", chargeInmAh);
        }
    }
}
