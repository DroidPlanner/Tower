package com.droidplanner.MAVLink;

import com.MAVLink.Messages.ApmModes;

public class DroneState {
	public boolean failsafe;
	public boolean armed;
	public ApmModes mode;

	public DroneState(boolean failsafe, boolean armed,
			ApmModes mode) {
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
}