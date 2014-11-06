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

    /**
     * This class contains functions used to generate a spline path.
     */
    public static class SplinePath {

        /**
         * Used as tag for logging.
         */
        private static final String TAG = SplinePath.class.getSimpleName();

        private final static int SPLINE_DECIMATION = 20;

        /**
         * Process the given map coordinates, and return a set of coordinates
         * describing the spline path.
         *
         * @param points
         *            map coordinates decimation factor
         * @return set of coordinates describing the spline path
         */
        public static List<LatLong> process(List<LatLong> points) {
            final int pointsCount = points.size();
            if (pointsCount < 4) {
                System.err.println("Not enough points!");
                return points;
            }

            final List<LatLong> results = processPath(points);
            results.add(0, points.get(0));
            results.add(points.get(pointsCount - 1));
            return results;
        }

        private static List<LatLong> processPath(List<LatLong> points) {
            final List<LatLong> results = new ArrayList<LatLong>();
            for (int i = 3; i < points.size(); i++) {
                results.addAll(processPathSegment(points.get(i - 3), points.get(i - 2),
                        points.get(i - 1), points.get(i)));
            }
            return results;
        }

        private static List<LatLong> processPathSegment(LatLong l1, LatLong l2, LatLong l3, LatLong l4) {
            Spline spline = new Spline(l1, l2, l3, l4);
            return spline.generateCoordinates(SPLINE_DECIMATION);
        }

    }

    public static class Spline {

        private static final double SPLINE_TENSION = 1.6;

        private LatLong p0;
        private LatLong p0_prime;
        private LatLong a;
        private LatLong b;

        public Spline(LatLong pMinus1, LatLong p0, LatLong p1, LatLong p2) {
            this.p0 = p0;

            // derivative at a point is based on difference of previous and next
            // points
            p0_prime = p1.subtract(pMinus1).dot(1 / SPLINE_TENSION);
            LatLong p1_prime = p2.subtract(this.p0).dot(1 / SPLINE_TENSION);

            // compute a and b coords used in spline formula
            a = LatLong.sum(this.p0.dot(2), p1.dot(-2), p0_prime, p1_prime);
            b = LatLong.sum(this.p0.dot(-3), p1.dot(3), p0_prime.dot(-2), p1_prime.negate());
        }

        public List<LatLong> generateCoordinates(int decimation) {
            ArrayList<LatLong> result = new ArrayList<LatLong>();
            float step = 1f / decimation;
            for (float i = 0; i < 1; i += step) {
                result.add(evaluate(i));
            }

            return result;
        }

        private LatLong evaluate(double t) {
            double tSquared = t * t;
            double tCubed = tSquared * t;

            return LatLong.sum(a.dot(tCubed), b.dot(tSquared), p0_prime.dot(t), p0);
        }

    }

}
