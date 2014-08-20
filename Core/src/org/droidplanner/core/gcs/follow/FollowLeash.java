package org.droidplanner.core.gcs.follow;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.gcs.location.Location;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.geoTools.GeoTools;
import org.droidplanner.core.helpers.units.Length;

public class FollowLeash extends FollowAlgorithm {

	public FollowLeash(Drone drone, Length radius) {
		super(drone, radius);
	}

	@Override
	public FollowModes getType() {
		return FollowModes.LEASH;
	}

	@Override
	public void processNewLocation(Location location) {
		if (GeoTools.getDistance(location.getCoord(), drone.GPS.getPosition()).valueInMeters() > radius
				.valueInMeters()) {
			double headingGCStoDrone = GeoTools.getHeadingFromCoordinates(location.getCoord(),
					drone.GPS.getPosition());
			Coord2D goCoord = GeoTools.newCoordFromBearingAndDistance(location.getCoord(), headingGCStoDrone,
					radius.valueInMeters());
			drone.guidedPoint.newGuidedCoord(goCoord);
		}
	}

}
