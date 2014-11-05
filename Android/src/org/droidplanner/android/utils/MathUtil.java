package org.droidplanner.android.utils;

import com.ox3dr.services.android.lib.coordinate.LatLng;

/**
 * Created by fhuya on 11/4/14.
 */
public class MathUtil {

    /**
     * Radius of the earth in meters.
     * Source: WGS84
     */
    private static final double RADIUS_OF_EARTH = 6378137.0;

    /**
     * Computes the distance between two coordinates
     *
     * @return distance in meters
     */
    public static double getDistance(LatLng from, LatLng to) {
        return RADIUS_OF_EARTH * Math.toRadians(getArcInRadians(from, to));
    }

    /**
     * Calculates the arc between two points
     * http://en.wikipedia.org/wiki/Haversine_formula
     *
     * @return the arc in degrees
     */
    static double getArcInRadians(LatLng from, LatLng to) {

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
}
