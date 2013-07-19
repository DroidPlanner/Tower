package com.droidplanner.MAVLink;

import com.MAVLink.Messages.ApmModes;

public class DroneState extends DroneVariable {
	public boolean failsafe = false;
	public boolean armed = false;
	public ApmModes mode = ApmModes.UNKNOWN;

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
}