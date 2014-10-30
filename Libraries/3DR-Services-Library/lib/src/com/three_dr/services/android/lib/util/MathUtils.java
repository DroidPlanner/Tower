package com.three_dr.services.android.lib.util;

import com.three_dr.services.android.lib.coordinate.LatLng;

/**
 * Created by fhuya on 10/8/14.
 */
public class MathUtils {

    private static final double RADIUS_OF_EARTH = 6378137.0;// In meters. Source: WGS84

    public static double getArcInRadians(LatLng start, LatLng end){
        final double latitudeArc = (float) Math.toRadians(start.getLatitude() - end.getLatitude());
        final double longitudeArc = (float) Math.toRadians(start.getLongitude() - end.getLongitude
                ());

        double latitudeH = Math.sin(latitudeArc * 0.5);
        latitudeH *= latitudeH;

        double longitudeH = Math.sin(longitudeArc * 0.5);
        longitudeH *= longitudeH;

        double tmp = Math.cos(Math.toRadians(start.getLatitude()))
                * Math.cos(Math.toRadians(end.getLatitude()));
        return Math.toDegrees(2.0 * Math.asin(Math.sqrt(latitudeH + tmp * longitudeH)));
     }

    public static double getDistance(LatLng from, LatLng to){
        return RADIUS_OF_EARTH * Math.toRadians(getArcInRadians(from, to));
    }
}
