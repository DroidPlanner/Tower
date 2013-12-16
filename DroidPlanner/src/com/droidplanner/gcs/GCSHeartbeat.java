package com.droidplanner.gcs;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.droidplanner.MAVLink.MavLinkHeartbeat;
import com.droidplanner.drone.Drone;

/**
 * This class is used to send periodic heartbeat messages to the drone.
 */
public class GCSHeartbeat {

	/**
	 * This is the drone to send the heartbeat message to.
	 */
	private final Drone drone;

	/**
	 * This is the heartbeat period in seconds.
	 */
	private final int period;

	/**
	 * ScheduledExecutorService used to periodically schedule the heartbeat.
	 */
	private ScheduledExecutorService heartbeatExecutor;

	public GCSHeartbeat(Drone drone, int freqHz) {
		this.drone = drone;
		this.period = freqHz;
	}

	/**
	 * Set the state of the heartbeat.
	 * 
	 * @param active
	 *            true to activate the heartbeat, false to deactivate it
	 */
	public void setActive(boolean active) {
		if (active) {
			heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
			heartbeatExecutor.scheduleWithFixedDelay(new Runnable() {
				@Override
				public void run() {
					MavLinkHeartbeat.sendMavHeartbeat(drone);
				}
			}, 0, period, TimeUnit.SECONDS);
		} else if (heartbeatExecutor != null) {
			heartbeatExecutor.shutdownNow();
			heartbeatExecutor = null;
		}
	}
}
