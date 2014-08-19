package org.droidplanner.android.gcs.roi;

import org.droidplanner.android.gcs.location.LocationReceiver;
import org.droidplanner.core.MAVLink.MavLinkROI;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.Handler;
import org.droidplanner.core.drone.variables.ROIPoint;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.helpers.geoTools.GeoTools;
import org.droidplanner.core.helpers.units.Altitude;

import android.location.Location;

/**
 * Uses location data from Android's FusedLocation LocationManager at 1Hz and
 * calculates new points at 10Hz based on Last Location and Last Velocity.
 *
 */
public class ROIEstimator implements LocationReceiver {

    private static final int TIMEOUT = 100;
    private Location realLocation;
    private long timeOfLastLocation;

    private Drone drone;
    private Handler watchdog;
    private ROIPoint rOIPoint;

    public Runnable watchdogCallback = new Runnable() {
        @Override
        public void run() {
            updateROI();
        }

    };

    public ROIEstimator(Handler handler, Drone drone) {
        this.watchdog = handler;
        this.drone = drone;
        rOIPoint = new ROIPoint(drone);
    }

    public void disableLocationUpdates() {
        watchdog.removeCallbacks(watchdogCallback);
    }

    @Override
    public void onLocationChanged(Location location) {
        disableLocationUpdates();
        realLocation = location;
        timeOfLastLocation = System.currentTimeMillis();
        updateROI();
    }

    private void updateROI() {
        if (realLocation == null) {
            return;
        }

        Coord2D gcsCoord = new Coord2D(realLocation.getLatitude(),
                realLocation.getLongitude());

        float bearing = 0;
        if(realLocation.hasBearing()){
            bearing = realLocation.getBearing();
        }

        float distanceTraveledSinceLastPoint = realLocation.getSpeed()
                * (System.currentTimeMillis() - timeOfLastLocation) / 1000f;

        if (distanceTraveledSinceLastPoint > 0.0) {
            Coord2D goCoord = GeoTools.newCoordFromBearingAndDistance(gcsCoord,
                    bearing, distanceTraveledSinceLastPoint);
            rOIPoint.setROICoord(goCoord);
        }

        watchdog.postDelayed(watchdogCallback, TIMEOUT);
    }
}
