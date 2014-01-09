package org.droidplanner.drone.variables;

import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneVariable;

import android.os.Handler;

public class HeartBeat extends DroneVariable {

	public HeartBeat(Drone myDrone) {
		super(myDrone);
	}

	private static long HEARTBEAT_NORMAL_TIMEOUT = 5000;
	private static long HEARTBEAT_LOST_TIMEOUT = 15000;
	
	public HeartbeatState heartbeatState;
	public Handler watchdog = new Handler();
	public Runnable watchdogCallback = new Runnable() {
		@Override
		public void run() {
			onHeartbeatTimeout();
		}
	};
	enum HeartbeatState {
		FIRST_HEARTBEAT, LOST_HEARTBEAT, NORMAL_HEARTBEAT
	}

	public void onHeartbeat() {

		switch (heartbeatState) {
		case FIRST_HEARTBEAT:
			myDrone.tts.speak("Connected");
			break;

		case LOST_HEARTBEAT:
			if (!Calibration.isCalibrating())
				myDrone.tts.speak("Data link restored");
			break;
		case NORMAL_HEARTBEAT:
			break;
		}

		heartbeatState = HeartbeatState.NORMAL_HEARTBEAT;
		restartWatchdog(HEARTBEAT_NORMAL_TIMEOUT);
	}

	private void onHeartbeatTimeout() {
		if (Calibration.isCalibrating()) {
			//drone.events.notifyDroneEvent(DroneEventsType.CALIBRATION_TIMEOUT);
		} else
			myDrone.tts.speak("Data link lost, check connection.");
		heartbeatState = HeartbeatState.LOST_HEARTBEAT;
		restartWatchdog(HEARTBEAT_LOST_TIMEOUT);
	}

	private void restartWatchdog(long timeout) {
		// re-start watchdog
		watchdog.removeCallbacks(watchdogCallback);
		watchdog.postDelayed(watchdogCallback, timeout);
	}

	public void notifyConnected() {
		heartbeatState = HeartbeatState.FIRST_HEARTBEAT;
		restartWatchdog(HEARTBEAT_NORMAL_TIMEOUT);
	}
}