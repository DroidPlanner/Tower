package org.droidplanner.android.helpers.calibration;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.parameters.Parameter;

public class CalParameters {
	private Drone myDrone;
	protected List<String> calParameterNames = new ArrayList<String>();
	protected List<Parameter> calParameterItems = new ArrayList<Parameter>();
	private boolean isUpdating = false;
	private OnCalibrationEvent listener;
	private int paramCount = 0;
	private int uploadIndex = 0;

	public interface OnCalibrationEvent {
		public void onReadCalibration(CalParameters calParameters);

		public void onSentCalibration(CalParameters calParameters);

		public void onCalibrationData(CalParameters calParameters, int index,
				int count, boolean isSending);
	}

	public CalParameters(Drone myDrone) {
		this.myDrone = myDrone;
	}

	public void setOnCalibrationEventListener(OnCalibrationEvent listener) {
		this.listener = listener;
	}

	public void processReceivedParam() {
		if (myDrone == null) {
			return;
		}
		Parameter param = myDrone.parameters.getLastParameter();
		if (isUpdating) {
			compareCalibrationParameter(param);
		} else {
			calParameterItems.add(param);
			paramCount = calParameterItems.size();
			readCalibrationParameter(calParameterItems.size());
		}
	}

	private void compareCalibrationParameter(Parameter param) {
		Parameter paramRef = calParameterItems.get(uploadIndex);

		if (paramRef.name.equalsIgnoreCase(param.name)
				&& paramRef.value == param.value) {
			uploadIndex++;
		}
		sendCalibrationParameters();
	}

	public void getCalibrationParameters(Drone drone) {
		this.myDrone = drone;
		calParameterItems.clear();
		paramCount = 0;
		readCalibrationParameter(0);
	}

	private void readCalibrationParameter(int seq) {
		if (seq >= calParameterNames.size()) {
			if (this.listener != null)
				this.listener.onReadCalibration(this);
			return;
		}

		if (myDrone != null)
			myDrone.parameters.ReadParameter(calParameterNames.get(seq));

		if (this.listener != null) {
			this.listener.onCalibrationData(this, seq,
					calParameterNames.size(), isUpdating);
		}
	}

	public void sendCalibrationParameters() {
		isUpdating = true;
		if (calParameterItems.size() > 0 && uploadIndex < paramCount) {
			if (this.listener != null) {
				this.listener.onCalibrationData(this, uploadIndex, paramCount,
						isUpdating);
			}
			if (myDrone != null) {
				myDrone.parameters.sendParameter(calParameterItems
						.get(uploadIndex));
			}
		} else {
			isUpdating = false;
			uploadIndex = 0;
			if (this.listener != null) {
				this.listener.onSentCalibration(this);
			}
		}
	}

	public boolean isParameterDownloaded() {
		return calParameterItems.size() == calParameterNames.size();
	}

	public double getParamValue(int paramIndex) {
		if (paramIndex >= calParameterItems.size())
			return -1;
		Parameter param = calParameterItems.get(paramIndex);
		return param.value;
	}

	public double getParamValueByName(String paramName) {
		for (Parameter param : calParameterItems) {
			if (param.name.contentEquals(paramName)) {
				return param.value;
			}
		}
		return -1;
	}

	public void setParamValue(int paramIndex, double value) {
		if (paramIndex >= calParameterItems.size())
			return;
		Parameter param = calParameterItems.get(paramIndex);
		param.value = value;
	}

	public void setParamValueByName(String paramName, double value) {
		for (Parameter param : calParameterItems) {
			if (param.name.contentEquals(paramName)) {
				param.value = value;
				return;
			}
		}
	}
}
