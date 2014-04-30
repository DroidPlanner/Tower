package org.droidplanner.core.drone.variables;

import org.droidplanner.core.MAVLink.MavLinkModes;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.Clock;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneVariable;

import com.MAVLink.Messages.ApmModes;

public class State extends DroneVariable {
	private boolean failsafe = false;
	private boolean armed = false;
	private boolean isFlying = false;
	private ApmModes mode = ApmModes.UNKNOWN;

	// flightTimer
	// ----------------
	private long startTime = 0;
	private long elapsedFlightTime = 0;
	private Clock clock;

	public State(Drone myDrone, Clock clock) {
		super(myDrone);
		this.clock = clock;
		resetFlightTimer();
	}

	public boolean isFailsafe() {
		return failsafe;
	}

	public boolean isArmed() {
		return armed;
	}

	public boolean isFlying() {
		return isFlying;
	}

	public ApmModes getMode() {
		return mode;
	}

	public void setIsFlying(boolean newState) {
		if (newState != isFlying) {
			isFlying = newState;
			myDrone.events.notifyDroneEvent(DroneEventsType.STATE);
			if (isFlying) {
				startTimer();
			} else {
				stopTimer();
			}
		}
	}

	public void setFailsafe(boolean newFailsafe) {
		if (this.failsafe != newFailsafe) {
			this.failsafe = newFailsafe;
			myDrone.events.notifyDroneEvent(DroneEventsType.FAILSAFE);
		}
	}

	public void setArmed(boolean newState) {
		if (this.armed != newState) {
			this.armed = newState;
			myDrone.events.notifyDroneEvent(DroneEventsType.ARMING);
			if (newState == true) {
				myDrone.waypointManager.getWaypoints();
			}
		}
	}

	public void setMode(ApmModes mode) {
		if (this.mode != mode) {
			this.mode = mode;
			myDrone.events.notifyDroneEvent(DroneEventsType.MODE);
		}
	}

	public void changeFlightMode(ApmModes mode) {
		if (ApmModes.isValid(mode)) {
			MavLinkModes.changeFlightMode(myDrone, mode);
		}
	}

	// flightTimer
	// ----------------

	public void resetFlightTimer() {
		elapsedFlightTime = 0;
		startTime = clock.elapsedRealtime();
	}

	public void startTimer() {
		startTime = clock.elapsedRealtime();
	}

	public void stopTimer() {
		// lets calc the final elapsed timer
		elapsedFlightTime += clock.elapsedRealtime() - startTime;
		startTime = clock.elapsedRealtime();
	}

	public long getFlightTime() {
		if (isFlying) {
			// calc delta time since last checked
			elapsedFlightTime += clock.elapsedRealtime() - startTime;
			startTime = clock.elapsedRealtime();
		}
		return elapsedFlightTime / 1000;
	}

}