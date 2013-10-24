package com.droidplanner.drone;

import com.droidplanner.parameters.Parameter;

import java.util.List;

public class DroneInterfaces {
	public interface MapUpdatedListner {
		public void onDroneUpdate();

		public void onDroneTypeChanged();
	}

	public interface DroneTypeListner {
		public void onDroneTypeChanged();
	}
	
	public interface InfoListner {
		public void onInfoUpdate();
	}
	
	public interface HomeDistanceChangedListner {
		public void onDistanceToHomeHasChanged();
	}
	
	public interface HudUpdatedListner {
		public void onOrientationUpdate();
		public void onSpeedAltitudeAndClimbRateUpdate();
	}

	public interface OnParameterManagerListner {
		public void onParameterReceived(Parameter parameter);

		public void onBeginReceivingParameters();
		public void onParameterReceived(Parameter parameter, int index, int count);
		public void onEndReceivingParameters(List<Parameter> parameter);
	}
}