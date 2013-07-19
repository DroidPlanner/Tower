package com.droidplanner.MAVLink;

public class DroneOrientation {
	public double roll;
	public double pitch;
	public double yaw;

	public DroneOrientation(double roll, double pitch, double yaw) {
		this.roll = roll;
		this.pitch = pitch;
		this.yaw = yaw;
	}
	
	public double getRoll() {
		return roll;
	}

	public double getPitch() {
		return pitch;
	}

	public double getYaw() {
		return yaw;
	}

}