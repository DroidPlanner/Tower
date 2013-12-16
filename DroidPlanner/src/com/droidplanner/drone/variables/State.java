package com.droidplanner.drone.variables;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.MAVLink.Messages.ApmModes;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_heartbeat;
import com.MAVLink.Messages.enums.MAV_MODE_FLAG;
import com.MAVLink.Messages.enums.MAV_STATE;
import com.droidplanner.MAVLink.MavLinkModes;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.ModeChangedListener;
import com.droidplanner.drone.DroneInterfaces.OnStateListner;
import com.droidplanner.drone.DroneVariable;

public class State extends DroneVariable {
	private boolean failsafe = false;
	private boolean armed = false;
	private boolean isFlying = false;
	private ApmModes mode = ApmModes.UNKNOWN;
	public List<OnStateListner> stateListner = new ArrayList<OnStateListner>();
	private List<ModeChangedListener> modeListner = new ArrayList<ModeChangedListener>();

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
		if (this.failsafe != newFailsafe) {
			this.failsafe = newFailsafe;
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
			myDrone.state.notifyModeChanged();

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

	public void addModeChangedListener(ModeChangedListener listner) {
		modeListner.add(listner);

	}

	public void removeFlightStateListner(OnStateListner listner) {
		if (stateListner.contains(listner)) {
			stateListner.remove(listner);
		}
	}

	public void removeModeListner(ModeChangedListener listner) {
		if (modeListner.contains(listner)) {
			modeListner.remove(listner);
		}
	}

	private void notifyFlightStateChanged() {
		for (OnStateListner listner : stateListner) {
			listner.onFlightStateChanged();
		}
	}

	private void notifyModeChanged() {
		for (ModeChangedListener listner : modeListner) {
			listner.onModeChanged();
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

	public void processState(msg_heartbeat msg_heart) {
		checkArmState(msg_heart);
		checkFailsafe(msg_heart);
		checkIfIsFlying(msg_heart);
	}

	private void checkFailsafe(msg_heartbeat msg_heart) {
		boolean failsafe2 = msg_heart.system_status == (byte) MAV_STATE.MAV_STATE_CRITICAL;
		setFailsafe(failsafe2);
	}

	private void checkArmState(msg_heartbeat msg_heart) {
		setArmed((msg_heart.base_mode & (byte) MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED) == (byte) MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED);
	}

	private void checkIfIsFlying(msg_heartbeat msg_heart) {
		switch (msg_heart.system_status) {
		case MAV_STATE.MAV_STATE_ACTIVE:
		case MAV_STATE.MAV_STATE_CRITICAL:
			setIsFlying(true);
			break;
		case MAV_STATE.MAV_STATE_STANDBY:
		case MAV_STATE.MAV_STATE_CALIBRATING:
			setIsFlying(false);
			break;
		}
	}

	@Override
	protected void processMAVLinkMessage(MAVLinkMessage msg) {
		switch (msg.msgid) {
		case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT:
			msg_heartbeat msg_heart = (msg_heartbeat) msg;
			processState(msg_heart);
			ApmModes newMode;
			newMode = ApmModes.getMode(msg_heart.custom_mode, msg_heart.type);
			setMode(newMode);
			break;
		}
	}
}