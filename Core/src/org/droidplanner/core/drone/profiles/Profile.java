package org.droidplanner.core.drone.profiles;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneVariable;

public class Profile extends DroneVariable {

	private VehicleProfile profile;

	public Profile(Drone myDrone) {
		super(myDrone);
	}

	public VehicleProfile getProfile() {
		return profile;
	}

	/*
	 * Load vehicle profile for current vehicle type
	 */
	public void load() {
		profile = myDrone.preferences.loadVehicleProfile(myDrone.type
				.getFirmwareType());
	}
}
