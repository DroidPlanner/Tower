package org.droidplanner.drone;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.drone.DroneInterfaces.OnDroneListner;


public class DroneEvents extends DroneVariable {

	public DroneEvents(Drone myDrone) {
		super(myDrone);
	}

	private List<OnDroneListner> droneListeners = new ArrayList<OnDroneListner>();

	public void addDroneListener(OnDroneListner listener) {
		if (listener != null & !droneListeners.contains(listener))
			droneListeners.add(listener);
	}

	public void removeDroneListener(OnDroneListner listener) {
		if (listener != null && droneListeners.contains(listener))
			droneListeners.remove(listener);
	}

	public void notifyDroneEvent(DroneEventsType event) {
		if (droneListeners.size() > 0) {
			for (OnDroneListner listener : droneListeners) {
				listener.onDroneEvent(event,myDrone);
			}
		}
	}
}
