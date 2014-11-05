package org.droidplanner.android.utils;

import java.util.Locale;

/**
 * Created by fhuya on 11/4/14.
 */
public class UnitUtil {

    public static class MetricUtil {
        public static String distanceToString(double distanceInMeters){
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
}
