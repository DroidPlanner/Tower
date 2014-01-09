package org.droidplanner.drone.variables;

import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.drone.DroneVariable;

import com.MAVLink.Messages.ardupilotmega.msg_heartbeat;

import android.os.Handler;

public class HeartBeat extends DroneVariable {

	private static long HEARTBEAT_NORMAL_TIMEOUT = 5000;
	private static long HEARTBEAT_LOST_TIMEOUT = 15000;
	
	public HeartbeatState heartbeatState;
	public int droneID = 1;

	enum HeartbeatState {
		FIRST_HEARTBEAT, LOST_HEARTBEAT, NORMAL_HEARTBEAT
	}
	
	public Handler watchdog = new Handler();
	public Runnable watchdogCallback = new Runnable() {
		@Override
		public void run() {
			onHeartbeatTimeout();
		}
	};

	public HeartBeat(Drone myDrone) {
		super(myDrone);
	}

	public void onHeartbeat(msg_heartbeat msg) {
		droneID = msg.sysid;
		
		switch (heartbeatState) {
		case FIRST_HEARTBEAT:
			myDrone.events.notifyDroneEvent(DroneEventsType.HEARTBEAT_FIRST);
			break;
		case LOST_HEARTBEAT:
			myDrone.events.notifyDroneEvent(DroneEventsType.HEARTBEAT_RESTORED);
			break;
		case NORMAL_HEARTBEAT:
			break;
		}

		heartbeatState = HeartbeatState.NORMAL_HEARTBEAT;
		restartWatchdog(HEARTBEAT_NORMAL_TIMEOUT);
	}

	public void notifyConnected() {
		heartbeatState = HeartbeatState.FIRST_HEARTBEAT;
		restartWatchdog(HEARTBEAT_NORMAL_TIMEOUT);
	}

	public void notifiyDisconnected() {
		watchdog.removeCallbacks(watchdogCallback);
	}

	private void onHeartbeatTimeout() {
		if (Calibration.isCalibrating()) {
			myDrone.events.notifyDroneEvent(DroneEventsType.CALIBRATION_TIMEOUT);
		}
		heartbeatState = HeartbeatState.LOST_HEARTBEAT;
		restartWatchdog(HEARTBEAT_LOST_TIMEOUT);
		myDrone.events.notifyDroneEvent(DroneEventsType.HEARTBEAT_TIMEOUT);
	}

	private void restartWatchdog(long timeout) {
		// re-start watchdog
		watchdog.removeCallbacks(watchdogCallback);
		watchdog.postDelayed(watchdogCallback, timeout);
	}
}