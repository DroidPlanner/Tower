package org.droidplanner.core.drone.variables;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.Handler;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.drone.DroneVariable;

import com.MAVLink.Messages.ardupilotmega.msg_heartbeat;

public class HeartBeat extends DroneVariable implements OnDroneListener {

	private static final long HEARTBEAT_NORMAL_TIMEOUT = 5000;
	private static final long HEARTBEAT_LOST_TIMEOUT = 15000;

    public static final byte INVALID_MAVLINK_VERSION = -1;

	public HeartbeatState heartbeatState = HeartbeatState.FIRST_HEARTBEAT;;
	public int droneID = 1;

    /**
     * Stores the version of the mavlink protocol.
     */
    private byte mMavlinkVersion = INVALID_MAVLINK_VERSION;

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

    /**
     * @return the version of the mavlink protocol.
     */
    public byte getMavlinkVersion(){
        return mMavlinkVersion;
    }

	public void onHeartbeat(msg_heartbeat msg) {
		droneID = msg.sysid;
        mMavlinkVersion = msg.mavlink_version;

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
        mMavlinkVersion = INVALID_MAVLINK_VERSION;
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
