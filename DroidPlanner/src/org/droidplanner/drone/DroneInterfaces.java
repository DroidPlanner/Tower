package org.droidplanner.drone;

import java.util.List;

import org.droidplanner.drone.variables.mission.WaypointEvent_Type;
import org.droidplanner.parameters.Parameter;


public class DroneInterfaces {

	public enum DroneEventsType {
		ORIENTATION, SPEED, BATTERY, GUIDEDPOINT, NAVIGATION, ATTIUTDE, RADIO,
		RC_IN, RC_OUT, ARMING, FAILSAFE, MODE, STATE,
		MISSION_UPDATE,MISSION_RECEIVED, TYPE, HOME, GPS, GPS_FIX, GPS_COUNT,
		PARAMETER, CALIBRATION_IMU, CALIBRATION_TIMEOUT, HEARTBEAT_TIMEOUT, HEARTBEAT_FIRST, HEARTBEAT_RESTORED, DISCONNECTED, CONNECTED;
	}

	public interface OnDroneListener {
		public void onDroneEvent(DroneEventsType event, Drone drone);
	}

	public interface OnParameterManagerListener {
		public void onBeginReceivingParameters();

		public void onParameterReceived(Parameter parameter, int index,
				int count);

		public void onEndReceivingParameters(List<Parameter> parameter);
	}

	public interface OnWaypointManagerListener {
		public void onBeginWaypointEvent(WaypointEvent_Type wpEvent);

		public void onWaypointEvent(WaypointEvent_Type wpEvent, int index,
				int count);

		public void onEndWaypointEvent(WaypointEvent_Type wpEvent);

	}
}
