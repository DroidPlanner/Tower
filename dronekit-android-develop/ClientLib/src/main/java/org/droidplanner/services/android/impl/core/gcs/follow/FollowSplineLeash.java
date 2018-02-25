package org.droidplanner.services.android.impl.core.gcs.follow;

import android.os.Handler;

import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Gps;

import org.droidplanner.services.android.impl.core.drone.manager.MavLinkDroneManager;
import org.droidplanner.services.android.impl.core.gcs.location.Location;
import org.droidplanner.services.android.impl.core.helpers.geoTools.GeoTools;

/**
 * Created by fhuya on 1/5/15.
 */
public class FollowSplineLeash extends FollowWithRadiusAlgorithm {
    @Override
    public void processNewLocation(Location location) {
        final LatLongAlt userLoc = location.getCoord();

        final Gps droneGps = (Gps) drone.getAttribute(AttributeType.GPS);
        final LatLong droneLoc = droneGps.getPosition();

        if (userLoc == null || droneLoc == null)
            return;

        if (GeoTools.getDistance(userLoc, droneLoc) > radius) {
            double headingGCSToDrone = GeoTools.getHeadingFromCoordinates(userLoc, droneLoc);
            LatLong goCoord = GeoTools.newCoordFromBearingAndDistance(userLoc, headingGCSToDrone, radius);

            double speed = location.getSpeed();
            double bearing = location.getBearing();
            double bearingInRad = Math.toRadians(bearing);
            double xVel = speed * Math.cos(bearingInRad);
            double yVel = speed * Math.sin(bearingInRad);
            drone.getGuidedPoint().newGuidedCoordAndVelocity(goCoord, xVel, yVel, 0);
        }

    }

    @Override
    public FollowModes getType() {
        return FollowModes.SPLINE_LEASH;
    }

    public FollowSplineLeash(MavLinkDroneManager droneMgr, Handler handler, double length) {
        super(droneMgr, handler, length);
    }
}
