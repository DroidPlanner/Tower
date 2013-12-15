package com.droidplanner.drone;

import java.util.ArrayList;
import java.util.List;

import com.droidplanner.drone.DroneInterfaces.DroneEventsType;
import com.droidplanner.drone.DroneInterfaces.OnDroneListner;
import com.droidplanner.helpers.TTS;

public class DroneEvents extends DroneVariable {
	private TTS tts;

	public DroneEvents(Drone myDrone) {
		super(myDrone);
		tts = myDrone.tts;
	}

	private List<OnDroneListner> droneListeners = new ArrayList<OnDroneListner>();

	public void addDroneListener(OnDroneListner listener) {
		if (listener != null)
			droneListeners.add(listener);
	}

	public void removeDroneListener(OnDroneListner listener) {
		if (listener != null && droneListeners.contains(listener))
			droneListeners.remove(listener);
	}

	public void notifyDroneEvent(DroneEventsType event) {
		notifyViaTTS(event);
		if (droneListeners.size() > 0) {
			for (OnDroneListner listener : droneListeners) {
				listener.onDroneEvent(event);
			}
		}
	}

	/**
	 * Warn the user if needed via the TTS module
	 * 
	 * @param event
	 */
	void notifyViaTTS(DroneEventsType event) {
		if (tts != null) {
			switch (event) {
			case ARMING:
				tts.speakArmedState(myDrone.state.isArmed());
				break;
			case BATTERY:
				tts.batteryDischargeNotification(myDrone.battery
						.getBattRemain());
				break;
			case MODE:
				tts.speakMode(myDrone.state.getMode());
				break;
			default:
				break;
			}
		}

	}
}
