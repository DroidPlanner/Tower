package org.droidplanner.drone.variables;

import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.drone.DroneInterfaces.Handler;
import org.droidplanner.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.drone.DroneVariable;

import com.MAVLink.Messages.ardupilotmega.msg_heartbeat;

public class HeartBeat extends DroneVariable implements OnDroneListener {

	private static long HEARTBEAT_NORMAL_TIMEOUT = 5000;
	private static long HEARTBEAT_LOST_TIMEOUT = 15000;

	public HeartbeatState heartbeatState = HeartbeatState.FIRST_HEARTBEAT;;
	public int droneID = 1;

	enum HeartbeatState {
		FIRST_HEARTBEAT, LOST_HEARTBEAT, NORMAL_HEARTBEAT
	}

	public Handler watchdog;
	public Runnable watchdogCallback = new Runnable() {
		@Override
		public void run() {
			onHeartbeatTimeout();
		}
	};

	public HeartBeat(Drone myDrone, Handler handler) {
		super(myDrone);
		this.watchdog = handler;
		myDrone.events.addDroneListener(this);
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

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case CONNECTED:
			notifyConnected();
			break;
		case DISCONNECTED:
			notifiyDisconnected();
			break;
		default:
			break;
		}
	}

	private void notifyConnected() {
		restartWatchdog(HEARTBEAT_NORMAL_TIMEOUT);
	}

	private void notifiyDisconnected() {
		watchdog.removeCallbacks(watchdogCallback);
		heartbeatState = HeartbeatState.FIRST_HEARTBEAT;
	}

	private void onHeartbeatTimeout() {
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
