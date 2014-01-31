package org.droidplanner.calibration;

import org.droidplanner.drone.Drone;

public class FSB_CalParameters extends CalParameters {

	public FSB_CalParameters(Drone myDrone) {
		super(myDrone);
			calParameterNames.add("FS_BATT_ENABLE");
			calParameterNames.add("FS_BATT_VOLTAGE");
			calParameterNames.add("FS_BATT_MAH");
	}
}
