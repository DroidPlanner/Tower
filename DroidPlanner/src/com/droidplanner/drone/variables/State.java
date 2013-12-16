package com.droidplanner.drone.variables;

import com.MAVLink.Messages.ApmModes;
import com.droidplanner.MAVLink.MavLinkModes;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.DroneEventsType;
import com.droidplanner.drone.DroneVariable;

public class State extends DroneVariable {
	private boolean failsafe = false;
	private boolean armed = false;
	private boolean isFlying = false;
	private ApmModes mode = ApmModes.UNKNOWN;

	public State(Drone myDrone) {
		super(myDrone);
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
			}
		}
	}

	public void changeFlightMode(ApmModes mode) {
		if (ApmModes.isValid(mode)) {
			MavLinkModes.changeFlightMode(myDrone, mode);
		}
	}
}