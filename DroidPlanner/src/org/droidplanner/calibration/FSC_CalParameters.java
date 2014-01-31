package org.droidplanner.calibration;

import org.droidplanner.drone.Drone;

public class FSC_CalParameters extends CalParameters {

	public FSC_CalParameters(Drone myDrone) {
		super(myDrone);
			calParameterNames.add("FS_GCS_ENABLE");
			calParameterNames.add("GCS_SYSID");
	}
}
