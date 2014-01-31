package org.droidplanner.calibration;

import org.droidplanner.drone.Drone;

public class FSF_CalParameters extends CalParameters {

	public FSF_CalParameters(Drone myDrone) {
		super(myDrone);
			calParameterNames.add("FENCE_ENABLE");
			calParameterNames.add("FENCE_TYPE");
			calParameterNames.add("FENCE_ACTION");
			calParameterNames.add("FENCE_MARGIN");
			calParameterNames.add("FENCE_RADIUS");
			calParameterNames.add("FENCE_ALT_MAX");
	}
}
