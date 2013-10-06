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
		public boolean onParameterReceived(Parameter parameter); //return true if needs to be process further
	}
}