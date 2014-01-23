package org.droidplanner.calibration;

import org.droidplanner.drone.Drone;

public class FSG_CalParameters extends CalParameters {

	public FSG_CalParameters(Drone myDrone) {
		super(myDrone);
			calParameterNames.add("FS_GPS_ENABLE");
			calParameterNames.add("FS_GCS_ENABLE");
			calParameterNames.add("GPS_HDOP_GOOD");
			calParameterNames.add("GCS_SYSID");
	}
}
