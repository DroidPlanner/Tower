package com.droidplanner.drone.variables;

import android.util.Log;

import com.MAVLink.Messages.ApmModes;
import com.droidplanner.MAVLink.MavLinkModes;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneVariable;

public class State extends DroneVariable {
	private boolean failsafe = false;
	private boolean armed = false;
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
		}
	}

	public void setMode(ApmModes mode) {
		if (this.mode != mode) {
			this.mode = mode;
			myDrone.tts.speakMode(mode);
			myDrone.notifyModeChanged();
		}
	}

	public void changeFlightMode(ApmModes mode) {
		Log.d("MODE", "mode "+mode.getName());
		if (ApmModes.isValid(mode)) {
			Log.d("MODE", "mode "+mode.getName()+" is valid");
			MavLinkModes.changeFlightMode(myDrone, mode);
		}
	}
}