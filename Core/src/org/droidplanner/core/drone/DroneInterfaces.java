package org.droidplanner.core.drone;

import java.util.List;

import org.droidplanner.core.MAVLink.WaypointManager;
import org.droidplanner.core.parameters.Parameter;

public class DroneInterfaces {

    /**
     * Sets of drone events used for broadcast throughout the app.
     */
    public enum DroneEventsType {
        /**
         *
         */
        ORIENTATION,

        /**
         *
         */
        SPEED,

        /**
         *
         */
        BATTERY,

        /**
         *
         */
        GUIDEDPOINT,

        /**
         *
         */
        NAVIGATION,

        /**
         *
         */
        ATTIUTDE,

        /**
         *
         */
        RADIO,

        /**
         *
         */
        RC_IN,

        /**
         *
         */
        RC_OUT,

        /**
         *
         */
        ARMING,

        /**
         *
         */
        FAILSAFE,

        /**
         *
         */
        MODE,

        /**
         *
         */
        STATE,

        /**
         *
         */
        MISSION_UPDATE,

        /**
         *
         */
        MISSION_RECEIVED,

        /**
         *
         */
        TYPE,

        /**
         *
         */
        HOME,

        /**
         *
         */
        GPS,

        /**
         *
         */
        GPS_FIX,

        /**
         *
         */
        GPS_COUNT,

        /**
         *
         */
        PARAMETER,

        /**
         *
         */
        CALIBRATION_IMU,

        /**
         *
         */
        CALIBRATION_TIMEOUT,

        /**
         *
         */
        HEARTBEAT_TIMEOUT,

        /**
         *
         */
        HEARTBEAT_FIRST,

        /**
         *
         */
        HEARTBEAT_RESTORED,

        /**
         *
         */
        DISCONNECTED,

        /**
         *
         */
        CONNECTED,

        /**
         *
         */
        MISSION_SENT,

        /**
         *
         */
        ARMING_STARTED,

        /**
         *
         */
        INVALID_POLYGON,

        /**
         *
         */
        MISSION_WP_UPDATE;
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
		public void onBeginWaypointEvent(
				WaypointManager.WaypointEvent_Type wpEvent);

		public void onWaypointEvent(WaypointManager.WaypointEvent_Type wpEvent,
				int index, int count);

		public void onEndWaypointEvent(
				WaypointManager.WaypointEvent_Type wpEvent);
	}

	public interface OnTimeout {

		public void notifyTimeOut(int timeOutCount);

	}

	public interface Clock {

		long elapsedRealtime();

	}

	public interface Handler {

		void removeCallbacks(Runnable thread);

		void postDelayed(Runnable thread, long timeout);

	}

}
