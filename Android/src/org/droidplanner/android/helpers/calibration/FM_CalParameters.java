package org.droidplanner.android.helpers.calibration;

import org.droidplanner.core.drone.Drone;

public class FM_CalParameters extends CalParameters {

	public FM_CalParameters(Drone myDrone) {
		super(myDrone);
		for (int i = 1; i <= 6; i++) {
			calParameterNames.add("FLTMODE" + i);
		}
		calParameterNames.add("SIMPLE");
		calParameterNames.add("SUPER_SIMPLE");
	}

	public void setFMData(int[] data) {
		for (int i = 0; i < 8; i++) {
			setParamValue(i, data[i]);
		}
	}
}
