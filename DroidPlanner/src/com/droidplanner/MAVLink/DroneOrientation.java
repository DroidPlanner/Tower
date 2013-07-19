package com.droidplanner.MAVLink;

public class DroneOrientation extends DroneVariable {
	public double roll = 0;
	public double pitch = 0;
	public double yaw = 0;

	public DroneOrientation(Drone myDrone) {
		super(myDrone);
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