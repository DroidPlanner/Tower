package com.droidplanner.drone.variables;

import android.util.Log;

import com.MAVLink.Messages.ApmModes;
import com.droidplanner.MAVLink.MavLinkModes;
import com.droidplanner.drone.Drone;
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
			if(myDrone.stateListner!=null){
				myDrone.stateListner.onFlightStateChanged();
			}
		}
	}

	public void setFailsafe(boolean newFailsafe) {
		if(this.failsafe!=newFailsafe){
			this.failsafe=newFailsafe;
			if(myDrone.stateListner!=null){
				myDrone.stateListner.onFailsafeChanged();
			}
		}	
	}

	public void setArmed(boolean newState) {
		if (this.armed != newState) {
			myDrone.tts.speakArmedState(newState);
			this.armed = newState;
			if(myDrone.stateListner!=null){
				myDrone.stateListner.onArmChanged();
			}			
		}
	}

	public void setMode(ApmModes mode) {
		if (this.mode != mode) {
			this.mode = mode;
			myDrone.tts.speakMode(mode);
			myDrone.notifyModeChanged();

			if (getMode() != ApmModes.ROTOR_GUIDED) {
				myDrone.guidedPoint.invalidateCoord();
			}
		}
	}

	public void changeFlightMode(ApmModes mode) {
		Log.d("MODE", "mode " + mode.getName());
		if (ApmModes.isValid(mode)) {
			Log.d("MODE", "mode " + mode.getName() + " is valid");
			MavLinkModes.changeFlightMode(myDrone, mode);
		}
	}

}