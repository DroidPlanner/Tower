package org.droidplanner.android.helpers.calibration;

import org.droidplanner.core.drone.Drone;

public class RC_CalParameters extends CalParameters {

	public RC_CalParameters(Drone myDrone) {
		super(myDrone);
		for (int i = 1; i <= 8; i++) {
			calParameterNames.add("RC" + i + "_MIN");
			calParameterNames.add("RC" + i + "_MAX");
			calParameterNames.add("RC" + i + "_TRIM");
		}
	}

	public void setRCData(int[] minData, int[] midData, int[] maxData) {
		for (int i = 0; i < 8; i++) {
			setParamValue((3 * i), minData[i]);
			setParamValue((3 * i) + 1, midData[i]);
			setParamValue((3 * i) + 2, maxData[i]);
		}
	}
}
