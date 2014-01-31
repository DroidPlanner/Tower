package org.droidplanner.calibration;

import org.droidplanner.drone.Drone;

public class FSG_CalParameters extends CalParameters {

	public FSG_CalParameters(Drone myDrone) {
		super(myDrone);
			calParameterNames.add("FS_GPS_ENABLE");
			calParameterNames.add("GPS_HDOP_GOOD");
			calParameterNames.add("GPSGLITCH_ENABLE");
			calParameterNames.add("GPSGLITCH_RADIUS");
			calParameterNames.add("GPSGLITCH_ACCEL");
	}
}
