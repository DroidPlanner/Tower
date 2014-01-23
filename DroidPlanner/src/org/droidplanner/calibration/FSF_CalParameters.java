package org.droidplanner.calibration;

import org.droidplanner.drone.Drone;

public class FSF_CalParameters extends CalParameters {

	public FSF_CalParameters(Drone myDrone) {
		super(myDrone);
			calParameterNames.add("FS_GPS_ENABLE");
			calParameterNames.add("GPS_HDOP_GOOD");
	}
}
