package com.droidplanner.drone;

import com.droidplanner.parameters.Parameter;

public class DroneInterfaces {
	public interface MapUpdatedListner {
		public void onDroneUpdate();
	}

	public interface DroneTypeListner {
		public void onDroneTypeChanged();
	}

	public interface HudUpdatedListner {
		public void onDroneUpdate();
	}

	public interface OnParameterManagerListner {
		public void onParameterReceived(Parameter parameter);
	}
}