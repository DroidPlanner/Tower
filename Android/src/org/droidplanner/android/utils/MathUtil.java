package org.droidplanner.android.utils;

import com.ox3dr.services.android.lib.coordinate.LatLong;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fhuya on 11/4/14.
 */
public class MathUtil {

    /**
     * Radius of the earth in meters.
     * Source: WGS84
     */
    private static final double RADIUS_OF_EARTH = 6378137.0;

    public static final int SIGNAL_MAX_FADE_MARGIN = 50;
    public static final int SIGNAL_MIN_FADE_MARGIN = 6;

    /**
     * Computes the distance between two coordinates
     *
     * @return distance in meters
     */
    public static double getDistance(LatLong from, LatLong to) {
        return RADIUS_OF_EARTH * Math.toRadians(getArcInRadians(from, to));
    }

    /**
     * Calculates the arc between two points
     * http://en.wikipedia.org/wiki/Haversine_formula
     *
     * @return the arc in degrees
     */
    static double getArcInRadians(LatLong from, LatLong to) {

        double latitudeArc = Math.toRadians(from.getLatitude() - to.getLatitude());
        double longitudeArc = Math.toRadians(from.getLongitude() - to.getLongitude());

        double latitudeH = Math.sin(latitudeArc * 0.5);
        latitudeH *= latitudeH;
        double lontitudeH = Math.sin(longitudeArc * 0.5);
        lontitudeH *= lontitudeH;

        double tmp = Math.cos(Math.toRadians(from.getLatitude()))
                * Math.cos(Math.toRadians(to.getLatitude()));
        return Math.toDegrees(2.0 * Math.asin(Math.sqrt(latitudeH + tmp * lontitudeH)));
    }

    /**
     * Signal Strength in percentage
     *
     * @return percentage
     */
    public static int getSignalStrength(double fadeMargin, double remFadeMargin) {
        return (int) (MathUtil.Normalize(Math.min(fadeMargin, remFadeMargin),
                SIGNAL_MIN_FADE_MARGIN, SIGNAL_MAX_FADE_MARGIN) * 100);
    }

    private static double Constrain(double value, double min, double max) {
        value = Math.max(value, min);
        value = Math.min(value, max);
        return value;
    }

    public static double Normalize(double value, double min, double max) {
        value = Constrain(value, min, max);
        return (value - min) / (max - min);

    }

    /**
     * Based on the Ramer–Douglas–Peucker algorithm algorithm
     * http://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm
     */
    public static List<LatLong> simplify(List<LatLong> list, double tolerance) {
        int index = 0;
        double dmax = 0;
        int lastIndex = list.size() - 1;

        // Find the point with the maximum distance
        for (int i = 1; i < lastIndex; i++) {
            double d = pointToLineDistance(list.get(0), list.get(lastIndex), list.get(i));
            if (d > dmax) {
                index = i;
                dmax = d;
            }
        }

        // If max distance is greater than epsilon, recursively simplify
        List<LatLong> ResultList = new ArrayList<LatLong>();
        if (dmax > tolerance) {
            // Recursive call
            List<LatLong> recResults1 = simplify(list.subList(0, index + 1), tolerance);
            List<LatLong> recResults2 = simplify(list.subList(index, lastIndex + 1), tolerance);

            // Build the result list
            recResults1.remove(recResults1.size() - 1);
            ResultList.addAll(recResults1);
            ResultList.addAll(recResults2);
        } else {
            ResultList.add(list.get(0));
            ResultList.add(list.get(lastIndex));
        }

        // Return the result
        return ResultList;
    }

    /**
     * Provides the distance from a point P to the line segment that passes
     * through A-B. If the point is not on the side of the line, returns the
     * distance to the closest point
     *
     * @param L1
     *            First point of the line
     * @param L2
     *            Second point of the line
     * @param P
     *            Point to measure the distance
     */
    public static double pointToLineDistance(LatLong L1, LatLong L2, LatLong P) {
        double A = P.getLatitude() - L1.getLatitude();
        double B = P.getLongitude() - L1.getLongitude();
        double C = L2.getLatitude() - L1.getLatitude();
        double D = L2.getLongitude() - L1.getLongitude();

        double dot = A * C + B * D;
        double len_sq = C * C + D * D;
        double param = dot / len_sq;

        double xx, yy;

        if (param < 0) // point behind the segment
        {
            xx = L1.getLatitude();
            yy = L1.getLongitude();
        } else if (param > 1) // point after the segment
        {
            xx = L2.getLatitude();
            yy = L2.getLongitude();
        } else { // point on the side of the segment
            xx = L1.getLatitude() + param * C;
            yy = L1.getLongitude() + param * D;
        }

        return Math.hypot(xx - P.getLatitude(), yy - P.getLongitude());
    }
}
