package org.droidplanner.core.gcs.follow;

import org.droidplanner.core.gcs.location.Location;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.geoTools.GeoTools;
import org.droidplanner.core.helpers.math.MathUtil;
import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.model.Drone;


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
		Coord2D gcsCoord = new Coord2D(location.getCoord().getLat(), location.getCoord().getLng());
		double bearing = location.getBearing();

		Coord2D goToCoord;
		if (GeoTools.getDistance(gcsCoord, super.drone.getGps().getPosition()).valueInMeters() > super.radius
				.valueInMeters()) {
			double headingGCStoDrone = GeoTools.getHeadingFromCoordinates(gcsCoord,
					super.drone.getGps().getPosition());
			double userRigthHeading = 90.0 + bearing;
			double alpha = MathUtil.Normalize(location.getSpeed(), 0.0, TOP_SPEED);
			double mixedHeading = MathUtil.bisectAngle(headingGCStoDrone, userRigthHeading, alpha);
			goToCoord = GeoTools.newCoordFromBearingAndDistance(gcsCoord, mixedHeading,
					super.radius.valueInMeters());
		} else {
			goToCoord = super.drone.getGuidedPoint().getCoord();
		}

		super.drone.getGuidedPoint().newGuidedCoord(goToCoord);
	}
}
