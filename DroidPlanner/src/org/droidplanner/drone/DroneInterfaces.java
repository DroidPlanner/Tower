package org.droidplanner.drone;

import java.util.List;

import org.droidplanner.drone.variables.mission.WaypointEvent_Type;
import org.droidplanner.parameters.Parameter;


public class DroneInterfaces {

    /**
     * Type of drone events generated.
     */
	public enum DroneEventsType {
        /**
         * Event sent when the drone's arming status changes.
         */
        ARMING,
        ATTIUTDE,

        /**
         * Event sent when information about the drone battery is updated.
         */
        BATTERY,
        GUIDEDPOINT,
        NAVIGATION,

        RADIO,
		RC_IN,
        RC_OUT,

        FAILSAFE,

        /**
         * Event sent when the drone apm mode (flight mode) changes.
         */
        MODE,
        STATE,
		MISSION_UPDATE,
        MISSION_RECEIVED,

        ORIENTATION,
        SPEED,

        /**
         * Event sent when the drone type is updated (fixed wing, helicopter, etc...).
         */
        TYPE,
        HOME,

        /**
         * Event fired when the drone gps position is updated.
         */
        GPS,

        /**
         * Event fired when the drone's gps count is updated.
         */
        GPS_COUNT,

        /**
         * Event fired when the drone gets a gps fix.
         */
        GPS_FIX,

		PARAMETER,
        CALIBRATION_IMU,
        CALIBRATION_TIMEOUT,
        HEARTBEAT_TIMEOUT,
        HEARTBEAT_FIRST,
        HEARTBEAT_RESTORED,

        /**
         * Event sent when connection with the drone is broken/lost.
         */
        DISCONNECTED,

        /**
         * Event sent when connection with the drone is established.
         */
        CONNECTED;
	}

	public interface OnDroneListner {
		public void onDroneEvent(DroneEventsType event, Drone drone);
	}

	public interface OnParameterManagerListner {
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