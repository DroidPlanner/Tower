package org.droidplanner.core.drone.variables;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneVariable;

public class Altitude extends DroneVariable {
	private double altitude = 0;
	private double targetAltitude = 0;

	public Altitude(Drone myDrone) {
		super(myDrone);
	}

	public double getAltitude() {
		return altitude;
	}

	public double getTargetAltitude() {
		return targetAltitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	public void setAltitudeError(double alt_error) {
		targetAltitude = alt_error + altitude;
	}

}