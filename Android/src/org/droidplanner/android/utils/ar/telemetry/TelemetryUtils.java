package org.droidplanner.android.utils.ar.telemetry;

import com.google.android.gms.maps.model.LatLng;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;

/**
 * Utility methods for AR telemetry calculations.
 */
public class TelemetryUtils {
    private static final double EARTH_RADIUS_IN_METERS = 6371010.0;
    private static final double DISTANCE_BETWEEN_LATITUDE_LINES_IN_METERS = 111319.5;

    // Calculate a Location from a start location, azimuth (in degrees), and distance.
    // This methods computes lat/long without altitude.
    public static LatLongAlt newLocationFromAzimuthAndDistance(LatLongAlt location, float azimuth, float distance) {
        double rLat = Math.toRadians(location.getLatitude());
        double rLong = Math.toRadians(location.getLongitude());
        double dist = (double) distance / EARTH_RADIUS_IN_METERS;
        double az = Math.toRadians((double) azimuth);

        double lat = Math.asin(Math.sin(rLat) * Math.cos(dist) + Math.cos(rLat) * Math.sin(dist) * Math.cos(az));

        double latitude = Math.toDegrees(lat);
        double longitude = Math.toDegrees(rLong + Math.atan2(Math.sin(az) * Math.sin(dist) * Math.cos(rLat),
            Math.cos(dist) - Math.sin(rLat) * Math.sin(lat)));

        return new LatLongAlt(latitude, longitude, 0.0);
    }

    // Calculate azimuth between a start and end point (in degrees).
    // See http://www.movable-type.co.uk/scripts/latlong.html
    public static double calcAzimuthFromPoints(LatLng start, LatLng end) {
        double rLat1 = Math.toRadians(start.latitude);
        double rLong1 = Math.toRadians(start.longitude);
        double rLat2 = Math.toRadians(end.latitude);
        double rLong2 = Math.toRadians(end.longitude);

        double y = Math.sin(rLong2 - rLong1) * Math.cos(rLat2);
        double x = Math.cos(rLat1) * Math.sin(rLat2) - Math.sin(rLat1) * Math.cos(rLat2) * Math.cos(rLong2 - rLong1);

        double rad = Math.atan2(y, x);

        // Convert to degrees, then normalize to (0, 360) range.
        double degrees = Math.toDegrees(rad);
        degrees = (degrees + 360.0) % 360.0;

        return degrees;
    }

    // Based on https://github.com/100grams/CoreLocationUtils. Returns distance between the given
    // points in meters.
    public static float getDistanceFromPoints(LatLng a, LatLng b) {
        // Get the difference between our two points then convert the difference into radians.
        double nDLat = Math.toRadians(a.latitude - b.latitude);
        double nDLon = Math.toRadians(a.longitude - b.longitude);

        double fromLat = Math.toRadians(b.latitude);
        double toLat = Math.toRadians(a.latitude);

        double lat2 = Math.sin(nDLat/2) * Math.sin(nDLat/2);
        double long2 = Math.sin(nDLon / 2) * Math.sin(nDLon / 2);
        double nA = lat2 + Math.cos(fromLat) * Math.cos(toLat) * long2;

        double nC = 2 * Math.atan2(Math.sqrt(nA), Math.sqrt( 1 - nA ));
        double nD = EARTH_RADIUS_IN_METERS * nC;

        return (float) nD;
    }

    // Same as above, except includes altitude in the calculation. This isn't the most efficient
    // calculation - it's just bolted on the 2d version.
    public static float getDistanceFromPoints3d(LatLongAlt a, LatLongAlt b) {
        double twoDDist = getDistanceFromPoints(new LatLng(a.getLatitude(), a.getLongitude()),
            new LatLng(b.getLatitude(), b.getLongitude()));
        double alt2 = (a.getAltitude() - b.getAltitude()) * (a.getAltitude() - b.getAltitude());
        double dist2 = twoDDist * twoDDist;

        return (float) Math.sqrt(alt2 + dist2);
    }

    // This function calculates the "cross-track" distance from pt to the great circle path from
    // start to end. Based on formula from here: http://www.movable-type.co.uk/scripts/latlong.html
    //
    // Formula:	dxt = asin( sin(δ13) ⋅ sin(θ13−θ12) ) ⋅ R
    // where	δ13 is (angular) distance from start point to third point
    // θ13 is (initial) bearing from start point to third point
    // θ12 is (initial) bearing from start point to end point
    // R is the earth’s radius.
    public static float getCrossTrackDistance(LatLng start, LatLng end, LatLng pt) {
        // Angular distance.
        double d13 = (double) (getDistanceFromPoints(start, pt)) / EARTH_RADIUS_IN_METERS;
        double t13 = Math.toRadians(calcAzimuthFromPoints(start, pt));
        double t12 = Math.toRadians(calcAzimuthFromPoints(start, end));

        double temp = Math.asin(Math.sin(d13) * Math.sin(t13 - t12));

        return (float) (temp * EARTH_RADIUS_IN_METERS);
    }

    // Convert location to vector3 using equirectangular approximation.
    public static Vector3 toVector3(LatLongAlt location) {
        double lat = Math.toRadians(location.getLatitude());
        float x = (float) (EARTH_RADIUS_IN_METERS * Math.toRadians(location.getLatitude()) * Math.cos(lat));
        float y = (float) (EARTH_RADIUS_IN_METERS * lat);

        return new Vector3(x , y, (float) location.getAltitude());
    }

    // Creates a vector3 from home to pos in our EUS camera space.
    public static Vector3 toCameraVec3(LatLongAlt home, LatLongAlt pos) {
        // You'll see (home - pos) throughout.  Same as -(pos - home)
        float z = (float) ((home.getLatitude() - pos.getLatitude()) * DISTANCE_BETWEEN_LATITUDE_LINES_IN_METERS);

        // Calculate longitude scaling factor.  We could cache this if necessary (Because we will not
        // likely fly far enough that this value changes) but we aren't doing so now.
        double scale = Math.abs(Math.cos(Math.toRadians(pos.getLatitude())));
        float x = (float) ((pos.getLongitude() - home.getLongitude()) *
            DISTANCE_BETWEEN_LATITUDE_LINES_IN_METERS * scale);
        double y = pos.getAltitude() - home.getAltitude();

        return new Vector3(x, (float) y, z);
    }

    // Add a EUS camera vector to the given location.
    public static LatLongAlt addCameraVec(LatLongAlt location, Vector3 vector) {
        LatLongAlt result = new LatLongAlt(location);
        result.setAltitude(vector.getY());
        double latDiff = (vector.getZ()) / DISTANCE_BETWEEN_LATITUDE_LINES_IN_METERS;
        result.setAltitude(result.getLatitude() - latDiff);
        double scale = Math.abs(Math.cos(Math.toRadians(result.getLatitude())));
        double longDiff = (vector.getX() / DISTANCE_BETWEEN_LATITUDE_LINES_IN_METERS) / scale;
        result.setLongitude(result.getLongitude() + longDiff);

        return result;
    }
}
