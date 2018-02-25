package org.droidplanner.services.android.impl.core.drone;

import org.droidplanner.services.android.impl.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.services.android.impl.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.services.android.impl.core.drone.autopilot.MavLinkDrone;

import java.util.concurrent.ConcurrentLinkedQueue;

public class DroneEvents extends DroneVariable<MavLinkDrone> {

    private final ConcurrentLinkedQueue<OnDroneListener> droneListeners = new ConcurrentLinkedQueue<OnDroneListener>();

    public DroneEvents(MavLinkDrone myDrone) {
        super(myDrone);
    }

    public void addDroneListener(OnDroneListener listener) {
        if (listener != null & !droneListeners.contains(listener))
            droneListeners.add(listener);
    }

    public void removeDroneListener(OnDroneListener listener) {
        if (listener != null && droneListeners.contains(listener))
            droneListeners.remove(listener);
    }

    public void removeAllDroneListeners(){
        droneListeners.clear();
    }

    public void notifyDroneEvent(DroneEventsType event) {
        if (event == null || droneListeners.isEmpty())
            return;

        for (OnDroneListener listener : droneListeners) {
            listener.onDroneEvent(event, myDrone);
        }
    }
}
