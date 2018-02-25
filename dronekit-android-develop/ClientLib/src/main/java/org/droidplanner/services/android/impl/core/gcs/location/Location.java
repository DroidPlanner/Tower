package org.droidplanner.services.android.impl.core.gcs.location;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;

public class Location {

    public interface LocationReceiver {
        void onLocationUpdate(Location location);

        void onLocationUnavailable();
    }

    public interface LocationFinder {
        void enableLocationUpdates(String tag, LocationReceiver receiver);
        void disableLocationUpdates(String tag);
    }

    private LatLongAlt coordinate;
    private double heading = 0.0;
    private double speed = 0.0;
    private boolean isAccurate;
    private long fixTime;

    public Location(LatLongAlt coord3d, float heading, float speed, boolean isAccurate, long fixTime) {
        coordinate = coord3d;
        this.heading = heading;
        this.speed = speed;
        this.isAccurate = isAccurate;
        this.fixTime = fixTime;
    }

    public LatLongAlt getCoord() {
        return coordinate;
    }

    public boolean isAccurate() {
        return !isInvalid() && this.isAccurate;
    }

    private boolean isInvalid(){
        return this.coordinate == null || (this.coordinate.getLatitude() == 0
                && this.coordinate.getLongitude() == 0);
    }

    public double getBearing() {
        return heading;
    }

    public double getSpeed() {
        return speed;
    }

    public long getFixTime() {
        return fixTime;
    }

}
