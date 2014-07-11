package org.droidplanner.android.gcs.follow;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.helpers.units.Length;

public class FollowLeft extends FollowHeadingAngle {

	public FollowLeft(Drone drone, Length radius) {
		super(drone, radius, -90.0);
	}

	@Override
	public FollowModes getType() {
		return FollowModes.LEFT;
	}

}
