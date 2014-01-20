package org.droidplanner.calibration;

import org.droidplanner.drone.Drone;

public class SF_CalParameters extends CalParameters {

	public SF_CalParameters(Drone myDrone) {
		super(myDrone);
			calParameterNames.add("CH7_OPT");
			calParameterNames.add("CH8_OPT");
			calParameterNames.add("TUNE");
			calParameterNames.add("TUNE_LOW");
			calParameterNames.add("TUNE_HIGH");
	}
}
