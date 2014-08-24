package org.droidplanner.core.drone.variables;

import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.model.Drone;

public class Altitude extends DroneVariable {
	private double altitude = 0;
	private double targetAltitude = 0;
	private double previousAltitude = 0;

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
		if(altitude > 121.92 &&  previousAltitude <= 121.92){
			myDrone.notifyDroneEvent(DroneInterfaces.DroneEventsType.WARNING_400FT_EXCEEDED);
		}
		previousAltitude = altitude;
	}

	public void setAltitudeError(double alt_error) {
		targetAltitude = alt_error + altitude;
	}

}