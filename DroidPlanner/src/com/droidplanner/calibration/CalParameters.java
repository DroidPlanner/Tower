package com.droidplanner.calibration;

import java.util.ArrayList;
import java.util.List;

import com.droidplanner.drone.Drone;
import com.droidplanner.parameters.Parameter;

public class CalParameters {
	private Drone myDrone;
	protected List<String> calParameterNames = new ArrayList<String>();
	protected List<Parameter> calParameterItems = new ArrayList<Parameter>();
	private boolean isUpdating = false;
	private OnCalibrationEvent listner;

	public interface OnCalibrationEvent {
		public void onReadCalibration(CalParameters calParameters);

		public void onSentCalibration(CalParameters calParameters);
	}

	public CalParameters(Drone myDrone) {
		this.myDrone = myDrone;
	}

	public void setOnCalibrationEventListener(OnCalibrationEvent listener) {
		this.listner = listener;
	}

	public void processReceivedParam() {
		if (myDrone == null) {
			return;
		}
		Parameter param = myDrone.parameters.getLastParameter();
		// Log.d("CAL", param.name +": " + String.valueOf(param.value));
		if (isUpdating) {
			compareCalibrationParameter(param);
		} else {
			calParameterItems.add(param);
			readCalibrationParameter(calParameterItems.size());
		}
	}

	private void compareCalibrationParameter(Parameter param) {
		Parameter paramRef = calParameterItems
				.get(calParameterItems.size() - 1);

		if (paramRef.name.equalsIgnoreCase(param.name)
				&& paramRef.value == param.value) {
			// Log.d("CAL", "Comp: " + paramRef.name +" : " + param.name);
			// Log.d("CAL", "Comp: " + String.valueOf(paramRef.value) +" : " +
			// String.valueOf(param.value));
			calParameterItems.remove(calParameterItems.size() - 1);
		}
		sendCalibrationParameters();
	}

	public void getCalibrationParameters(Drone drone) {
		this.myDrone = drone;
		calParameterItems.clear();
		readCalibrationParameter(0);
	}

	private void readCalibrationParameter(int seq) {
		if (seq >= calParameterNames.size()) {
			if (this.listner != null)
				this.listner.onSentCalibration(this);
			return;
		}
		if (myDrone != null)
			myDrone.parameters.ReadParameter(calParameterNames.get(seq));
	}

	public void sendCalibrationParameters() {
		isUpdating = true;
		if (calParameterItems.size() > 0 && myDrone != null) {
			myDrone.parameters.sendParameter(calParameterItems
					.get(calParameterItems.size() - 1));
		} else {
			isUpdating = false;
			if (this.listner != null)
				this.listner.onSentCalibration(this);
		}
	}
}
