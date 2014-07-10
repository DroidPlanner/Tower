package org.droidplanner.android.gcs.follow;

import org.droidplanner.android.gcs.follow.Follow.FollowModes;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.geoTools.GeoTools;
import org.droidplanner.core.helpers.math.MathUtil;
import org.droidplanner.core.helpers.units.Length;

import android.location.Location;

public class FollowCircle extends FollowType  {

	/**
	 * °/s
	 */
	private static final double circleRate = 20;
	private double circleAngle = 0.0;

	public FollowCircle(Drone drone, Length radius, double mIN_TIME_MS) {
		super(drone, radius, mIN_TIME_MS);
	}
	
	@Override
	public FollowModes getType() {
		return FollowModes.CIRCLE;
	}

	@Override
	public void processNewLocation(Location location) {
		Coord2D gcsCoord = new Coord2D(location.getLatitude(),
				location.getLongitude());
		Coord2D goCoord = GeoTools.newCoordFromBearingAndDistance(gcsCoord,
				circleAngle, radius.valueInMeters());
		circleAngle = MathUtil.constrainAngle(circleAngle + circleRate
				* super.MIN_TIME_MS / 1000.0);
		drone.guidedPoint.newGuidedCoord(goCoord);
	}
}
