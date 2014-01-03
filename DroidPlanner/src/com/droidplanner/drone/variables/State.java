package com.droidplanner.drone.variables;

import com.MAVLink.Messages.ApmModes;
import com.droidplanner.MAVLink.MavLinkModes;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.DroneEventsType;
import com.droidplanner.drone.DroneVariable;
import android.os.SystemClock;

public class State extends DroneVariable {
	private boolean failsafe = false;
	private boolean armed = false;
	private boolean isFlying = false;
	private ApmModes mode = ApmModes.UNKNOWN;

	// flightTimer
	// ----------------
	private long startTime = 0;
	private long elapsedFlightTime = 0;

	public State(Drone myDrone) {
		super(myDrone);
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
			if(isFlying){
				startTimer();
			}else{
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
		}
	}

	public void setMode(ApmModes mode) {
		if (this.mode != mode) {
			this.mode = mode;
			myDrone.events.notifyDroneEvent(DroneEventsType.MODE);
			if (getMode() != ApmModes.ROTOR_GUIDED) {
				myDrone.guidedPoint.invalidateCoord();
			}else{
				myDrone.guidedPoint.initCoord();
			}
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
		startTime = SystemClock.elapsedRealtime();
	}

	public void startTimer() {
		startTime = SystemClock.elapsedRealtime();
	}

	public void stopTimer() {
		// lets calc the final elapsed timer
		elapsedFlightTime 	+= SystemClock.elapsedRealtime() - startTime;
		startTime 			= SystemClock.elapsedRealtime();
	}

	public long getFlightTime() {
		if(isFlying){
			// calc delta time since last checked
			elapsedFlightTime 	+= SystemClock.elapsedRealtime() - startTime;
			startTime 			= SystemClock.elapsedRealtime();
		}
		return elapsedFlightTime / 1000;
	}

}