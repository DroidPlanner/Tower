package com.droidplanner.MAVLink;

public class DroneAltitude extends DroneVariable {
	public double altitude = 0;
	public double targetAltitude = 0;

	public DroneAltitude(Drone myDrone) {
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