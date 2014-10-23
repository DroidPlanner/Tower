package org.droidplanner.core.drone;

import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.model.Drone;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DroneEvents extends DroneVariable {

	private final ConcurrentLinkedQueue<DroneEventsType> eventsQueue = new ConcurrentLinkedQueue<DroneEventsType>();

	private final DroneInterfaces.Handler handler;

	private final Runnable eventsDispatcher = new Runnable() {
		@Override
		public void run() {
            do {
                handler.removeCallbacks(this);
                final DroneEventsType event = eventsQueue.poll();
                if (event != null && !droneListeners.isEmpty()) {
                    for (OnDroneListener listener : droneListeners) {
                        listener.onDroneEvent(event, myDrone);
                    }
                }
            }while(!eventsQueue.isEmpty());
		}
	};

	public DroneEvents(Drone myDrone, DroneInterfaces.Handler handler) {
		super(myDrone);
		this.handler = handler;
	}

	private final ConcurrentLinkedQueue<OnDroneListener> droneListeners = new ConcurrentLinkedQueue<OnDroneListener>();

	public void addDroneListener(OnDroneListener listener) {
		if (listener != null & !droneListeners.contains(listener))
			droneListeners.add(listener);
	}

	public void removeDroneListener(OnDroneListener listener) {
		if (listener != null && droneListeners.contains(listener))
			droneListeners.remove(listener);
	}

	public void notifyDroneEvent(DroneEventsType event) {
        eventsQueue.offer(event);
		handler.post(eventsDispatcher);
	}
}
