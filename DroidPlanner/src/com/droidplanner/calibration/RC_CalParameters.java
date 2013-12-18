package com.droidplanner.calibration;

import com.droidplanner.drone.Drone;

public class RC_CalParameters extends CalParameters {

	public RC_CalParameters(Drone myDrone) {
		super(myDrone);
		for(int i=1;i<=8;i++){
			calParameterNames.add("RC"+i+"_MIN");
			calParameterNames.add("RC"+i+"_MAX");
			calParameterNames.add("RC"+i+"_TRIM");
		}
	}
}
