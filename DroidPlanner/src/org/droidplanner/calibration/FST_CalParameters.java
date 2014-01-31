package org.droidplanner.calibration;

import org.droidplanner.drone.Drone;

public class FST_CalParameters extends CalParameters {

	public FST_CalParameters(Drone myDrone) {
		super(myDrone);
			calParameterNames.add("FS_THR_ENABLE");
			calParameterNames.add("FS_THR_VALUE");
	}
}
