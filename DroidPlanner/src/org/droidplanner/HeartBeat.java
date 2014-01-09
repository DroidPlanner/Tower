package org.droidplanner;

import org.droidplanner.drone.variables.Calibration;
import org.droidplanner.helpers.TTS;

import android.os.Handler;

public class HeartBeat {

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
	private TTS tts;

	enum HeartbeatState {
		FIRST_HEARTBEAT, LOST_HEARTBEAT, NORMAL_HEARTBEAT
	}

	public HeartBeat(TTS tts) {
		this.tts = tts;
	}

	public void onHeartbeat() {

		switch (heartbeatState) {
		case FIRST_HEARTBEAT:
			tts.speak("Connected");
			break;

		case LOST_HEARTBEAT:
			if (!Calibration.isCalibrating())
				tts.speak("Data link restored");
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
			tts.speak("Data link lost, check connection.");
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