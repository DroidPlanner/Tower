package org.droidplanner.drone.profiles;

import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneVariable;

public class Profile extends DroneVariable {

    private VehicleProfile profile;


    public Profile(Drone myDrone) {
        super(myDrone);
    }

    public VehicleProfile getProfile() {
        return profile;
    }

    /*
     * Load vehclie profile for current vehicle type
     */
    public void load() {
        profile = myDrone.preferences.loadVehicleProfile(myDrone.type.getFirmwareType());
    }
}
