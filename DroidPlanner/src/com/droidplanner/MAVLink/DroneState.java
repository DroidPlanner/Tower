package com.droidplanner.MAVLink;

import com.MAVLink.Messages.ApmModes;

public class DroneState extends DroneVariable {
	public boolean failsafe;
	public boolean armed;
	public ApmModes mode;

	public DroneState(Drone myDrone, boolean failsafe, boolean armed,
			ApmModes mode) {
		super(myDrone);
		this.failsafe = failsafe;
		this.armed = armed;
		this.mode = mode;
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