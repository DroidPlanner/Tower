package org.droidplanner.core.drone;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;

public class DroneEvents extends DroneVariable {

	public DroneEvents(Drone myDrone) {
		super(myDrone);
	}

	private List<OnDroneListener> droneListeners = new ArrayList<OnDroneListener>();

	public void addDroneListener(OnDroneListener listener) {
		if (listener != null & !droneListeners.contains(listener))
			droneListeners.add(listener);
	}

	public void removeDroneListener(OnDroneListener listener) {
		if (listener != null && droneListeners.contains(listener))
			droneListeners.remove(listener);
	}

	public void notifyDroneEvent(DroneEventsType event) {
		if (droneListeners.size() > 0) {
			for (OnDroneListener listener : droneListeners) {
				listener.onDroneEvent(event, myDrone);
			}
		}
	}
}
