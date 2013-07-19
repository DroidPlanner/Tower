package com.droidplanner.MAVLink;

public class DroneOrientation extends DroneVariable {
	public double roll;
	public double pitch;
	public double yaw;

	public DroneOrientation(Drone myDrone, double roll, double pitch, double yaw) {
		super(myDrone);
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

	public void setRollPitchYaw(double roll, double pitch, double yaw) {
		this.roll = roll;
		this.pitch = pitch;
		this.yaw = yaw;
		myDrone.notifyHudUpdate();
	}

}