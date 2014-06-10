package org.droidplanner.core.drone.variables;

import org.droidplanner.core.bus.events.DroneHeartBeatEvent;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.Handler;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.drone.DroneVariable;

import com.MAVLink.Messages.ardupilotmega.msg_heartbeat;

import de.greenrobot.event.EventBus;

public class HeartBeat extends DroneVariable implements OnDroneListener {

	private static long HEARTBEAT_NORMAL_TIMEOUT = 5000;
	private static long HEARTBEAT_LOST_TIMEOUT = 15000;

	public HeartbeatState heartbeatState = HeartbeatState.FIRST_HEARTBEAT;;
	public int droneID = 1;

	public enum HeartbeatState {
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

        final EventBus bus = EventBus.getDefault();

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

        //Broadcast the event
        bus.postSticky(new DroneHeartBeatEvent(msg, heartbeatState));

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
			notifyDisconnected();
			break;
		default:
			break;
		}
	}

	private void notifyConnected() {
		restartWatchdog(HEARTBEAT_NORMAL_TIMEOUT);
	}

	private void notifyDisconnected() {
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
