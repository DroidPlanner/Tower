package org.droidplanner.core.gcs.follow;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.gcs.location.Location;
import org.droidplanner.core.helpers.units.Length;

public class FollowAbove extends FollowAlgorithm {

	public FollowAbove(Drone drone, Length radius) {
		super(drone, radius);
	}

	@Override
	public FollowModes getType() {
		return FollowModes.ABOVE;
	}

	@Override
	public void processNewLocation(Location location) {
			drone.guidedPoint.newGuidedCoord(location.getCoord());
	}

}
