package org.droidplanner.core.drone;

import com.MAVLink.Messages.ardupilotmega.msg_heartbeat;

import org.droidplanner.core.drone.profiles.Parameters;
import org.droidplanner.core.drone.variables.GPS;
import org.droidplanner.core.drone.variables.State;
import org.droidplanner.core.firmware.FirmwareType;

/**
 * Defines the set of methods that drone implementations must support.
 */
public interface AbstractDrone {

    public void addDroneListener(DroneInterfaces.OnDroneListener listener);

    public void removeDroneListener(DroneInterfaces.OnDroneListener listener);

    public void notifyDroneEvent(DroneInterfaces.DroneEventsType event);

    public GPS getGps();

    public int getMavlinkVersion();

    public void onHeartbeat(msg_heartbeat msg);

    public State getState();

    public Parameters getParameters();

    public void setType(int type);

    public int getType();

    public FirmwareType getFirmwareType();
}
