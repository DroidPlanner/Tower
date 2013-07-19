package com.droidplanner.MAVLink;

public class DroneAltitude {
	public double altitude;
	public double targetAltitude;

	public DroneAltitude(double altitude, double targetAltitude) {
		this.altitude = altitude;
		this.targetAltitude = targetAltitude;
	}
	
	public double getAltitude() {
		return altitude;
	}
	public double getTargetAltitude() {
		return targetAltitude;
	}

}