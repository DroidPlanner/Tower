package com.droidplanner.calibration;

import com.droidplanner.drone.Drone;

public class CH_CalParameters extends CalParameters {

	public CH_CalParameters(Drone myDrone) {
		super(myDrone);
			calParameterNames.add("CH7_OPT");
			calParameterNames.add("CH8_OPT");
	}
}
