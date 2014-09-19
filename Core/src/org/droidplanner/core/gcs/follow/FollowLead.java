package org.droidplanner.core.gcs.follow;

import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.model.Drone;

public class FollowLead extends FollowHeadingAngle {

	public FollowLead(Drone drone, Length radius) {
		super(drone, radius, 0.0);
	}

	@Override
	public FollowModes getType() {
		return FollowModes.LEAD;
	}

}
