package org.droidplanner.core.drone;

/**
 * Defines the set of methods that drone implementations must support.
 */
public interface AbstractDrone {

    public void addDroneListener(DroneInterfaces.OnDroneListener listener);

    public void removeDroneListener(DroneInterfaces.OnDroneListener listener);

    public void notifyDroneEvent(DroneInterfaces.DroneEventsType event);
}
