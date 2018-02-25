package org.droidplanner.services.android.impl.core.gcs.follow;

import android.os.Handler;

import com.o3dr.services.android.lib.coordinate.LatLong;

import org.droidplanner.services.android.impl.core.drone.autopilot.MavLinkDrone;
import org.droidplanner.services.android.impl.core.drone.manager.MavLinkDroneManager;
import org.droidplanner.services.android.impl.core.gcs.location.Location;

/**
 * Created by fhuya on 1/5/15.
 */
public class FollowSplineAbove extends FollowAlgorithm {

    private final MavLinkDrone drone;

    @Override
    public void processNewLocation(Location location) {
        LatLong gcsLoc = new LatLong(location.getCoord());

        double speed = location.getSpeed();
        double bearing = location.getBearing();
        double bearingInRad = Math.toRadians(bearing);
        double xVel = speed * Math.cos(bearingInRad);
        double yVel = speed * Math.sin(bearingInRad);
        drone.getGuidedPoint().newGuidedCoordAndVelocity(gcsLoc, xVel, yVel, 0);
    }

    @Override
    public FollowModes getType() {
        return FollowModes.SPLINE_ABOVE;
    }

    public FollowSplineAbove(MavLinkDroneManager droneManager, Handler handler) {
        super(droneManager, handler);
        drone = droneManager.getDrone();
    }
}
