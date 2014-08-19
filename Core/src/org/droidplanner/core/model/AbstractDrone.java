package org.droidplanner.core.model;

import com.MAVLink.Messages.ardupilotmega.msg_heartbeat;

import org.droidplanner.core.MAVLink.MAVLinkStreams;
import org.droidplanner.core.MAVLink.WaypointManager;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.Preferences;
import org.droidplanner.core.drone.profiles.Parameters;
import org.droidplanner.core.drone.profiles.VehicleProfile;
import org.droidplanner.core.drone.variables.Altitude;
import org.droidplanner.core.drone.variables.Battery;
import org.droidplanner.core.drone.variables.GPS;
import org.droidplanner.core.drone.variables.Home;
import org.droidplanner.core.drone.variables.Navigation;
import org.droidplanner.core.drone.variables.Orientation;
import org.droidplanner.core.drone.variables.Radio;
import org.droidplanner.core.drone.variables.Speed;
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

    public void loadVehicleProfile();

    public VehicleProfile getVehicleProfile();

    public MAVLinkStreams.MAVLinkOutputStream getMavClient();

    public Preferences getPreferences();

    public WaypointManager getWaypointManager();

    public Speed getSpeed();

    public Battery getBattery();

    public Radio getRadio();

    public Home getHome();

    public Altitude getAltitude();

    public Orientation getOrientation();

    public Navigation getNavigation();
}
