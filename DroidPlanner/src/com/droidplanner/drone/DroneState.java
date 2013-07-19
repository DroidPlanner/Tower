package com.droidplanner.drone;

import com.MAVLink.waypoint;
import com.MAVLink.Messages.ApmModes;
import com.droidplanner.MAVLink.MavLinkModes;

public class DroneState extends DroneVariable {
	private boolean failsafe = false;
	private boolean armed = false;
	private ApmModes mode = ApmModes.UNKNOWN;

	public DroneState(Drone myDrone) {
		super(myDrone);
	}

	public boolean isFailsafe() {
		return failsafe;
	}

	public boolean isArmed() {
		return armed;
	}

	public ApmModes getMode() {
		return mode;
	}

	public void setArmedAndFailsafe(boolean armed, boolean failsafe) {
		if (this.armed != armed | this.failsafe != failsafe) {
			if (this.armed != armed) {
				myDrone.tts.speakArmedState(armed);
			}
			this.armed = armed;
			this.failsafe = failsafe;
			myDrone.notifyHudUpdate();
		}
	}

	public void setMode(ApmModes mode) {
		if (this.mode != mode) {
			this.mode = mode;
			myDrone.tts.speakMode(mode);
			myDrone.notifyHudUpdate();
		}
	}

	public void changeFlightMode(ApmModes mode) {
		MavLinkModes.changeFlightMode(myDrone.MavClient, mode);
	}

	public void setGuidedMode(waypoint waypoint) {
		MavLinkModes.setGuidedMode(myDrone.MavClient, waypoint);		
	}
}