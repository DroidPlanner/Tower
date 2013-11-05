package com.droidplanner.drone.variables;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.MAVLink.Messages.ApmModes;
import com.droidplanner.MAVLink.MavLinkModes;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.OnStateListner;
import com.droidplanner.drone.DroneVariable;

public class State extends DroneVariable {
	private boolean failsafe = false;
	private boolean armed = false;
	private boolean isFlying = false;
	private ApmModes mode = ApmModes.UNKNOWN;
	public List<OnStateListner> stateListner = new ArrayList<OnStateListner>();

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
			notifyFlightStateChanged();
		}
	}

	public void setFailsafe(boolean newFailsafe) {
		if(this.failsafe!=newFailsafe){
			this.failsafe=newFailsafe;
			notifyFailsafeChanged();
		}	
	}

	public void setArmed(boolean newState) {
		if (this.armed != newState) {
			this.armed = newState;
			notifyArmChanged();			
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
	
	public void addFlightStateListner(OnStateListner listner) {
		stateListner.add(listner);
	}
	
	public void removeFlightStateListner(OnStateListner listner) {
		if (stateListner.contains(listner)) {
			stateListner.remove(listner);			
		}
	}

	private void notifyFlightStateChanged() {
		for (OnStateListner listner : stateListner) {
			listner.onFlightStateChanged();			
		}
	}

	private void notifyFailsafeChanged() {
		for (OnStateListner listner : stateListner) {
			listner.onFailsafeChanged();			
		}
	}

	private void notifyArmChanged() {
		myDrone.tts.speakArmedState(armed);
		for (OnStateListner listner : stateListner) {
			listner.onArmChanged();			
		}
	}

}