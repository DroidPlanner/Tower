package com.droidplanner.MAVLink;

public class DroneAltitude extends DroneVariable{
	public double altitude;
	public double targetAltitude;

	public DroneAltitude(Drone myDrone,double altitude, double targetAltitude) {
		super(myDrone);
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