package org.droidplanner.android.utils.unit;

import java.util.Locale;

/**
 * Created by fhuya on 11/11/14.
 */
public class MetricUnitProvider implements UnitProvider {

    @Override
    public String areaToString(double areaInSqMeters) {
        if (areaInSqMeters >= 100000) {
            return String.format(Locale.US, "%2.1f km" + SQUARE_SYMBOL, areaInSqMeters / 1000000);
        } else if (areaInSqMeters >= 1) {
            return String.format(Locale.US, "%2.1f m" + SQUARE_SYMBOL, areaInSqMeters);
        } else if (areaInSqMeters >= 0.00001) {
            return String.format(Locale.US, "%2.2f cm" + SQUARE_SYMBOL, areaInSqMeters * 10000);
        } else {
            return areaInSqMeters + " m" + SQUARE_SYMBOL;
        }
    }

    @Override
    public String distanceToString(double distanceInMeters) {
        if (distanceInMeters >= 1000) {
            return String.format(Locale.US, "%2.1f km", distanceInMeters / 1000);
        } else if (distanceInMeters >= 0.1) {
            return String.format(Locale.US, "%2.1f m", distanceInMeters);
        } else if (distanceInMeters >= 0.001) {
            return String.format(Locale.US, "%2.1f mm", distanceInMeters * 1000);
        } else {
            return distanceInMeters + " m";
        }
    }
}
