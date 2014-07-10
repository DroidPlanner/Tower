package org.droidplanner.android.gcs.follow;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.geoTools.GeoTools;
import org.droidplanner.core.helpers.math.MathUtil;
import org.droidplanner.core.helpers.units.Length;

import android.location.Location;

public class FollowWakeboard extends FollowAlgorithm {

	private static final double TOP_SPEED = 5.0;

	public FollowWakeboard(Drone drone, Length radius) {
		super(drone, radius);
	}

	@Override
	public FollowModes getType() {
		return FollowModes.WAKEBOARD;
	}

	@Override
	public void processNewLocation(Location location) {
		Coord2D gcsCoord = new Coord2D(location.getLatitude(),
				location.getLongitude());
		float bearing = location.getBearing();

		Coord2D goToCoord;
		if (GeoTools.getDistance(gcsCoord, super.drone.GPS.getPosition())
				.valueInMeters() > super.radius.valueInMeters()) {
			double headingGCStoDrone = GeoTools.getHeadingFromCoordinates(
					gcsCoord, super.drone.GPS.getPosition());
			double userRigthHeading = 90.0 + bearing;
			double alpha = MathUtil.Normalize(location.getSpeed(), 0.0,
					TOP_SPEED);
			double mixedHeading = MathUtil.bisectAngle(headingGCStoDrone,
					userRigthHeading, alpha);
			goToCoord = GeoTools.newCoordFromBearingAndDistance(gcsCoord,
					mixedHeading, super.radius.valueInMeters());
		} else {
			goToCoord = super.drone.guidedPoint.getCoord();
		}

		super.drone.guidedPoint.newGuidedCoord(goToCoord);
	}
}
