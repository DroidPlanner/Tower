package org.droidplanner.core.drone;

import org.droidplanner.core.drone.variables.GPS;

/**
 * Defines the set of methods that drone implementations must support.
 */
public interface AbstractDrone {

    public void addDroneListener(DroneInterfaces.OnDroneListener listener);

    public void removeDroneListener(DroneInterfaces.OnDroneListener listener);

    public void notifyDroneEvent(DroneInterfaces.DroneEventsType event);

    public GPS getGps();
}
