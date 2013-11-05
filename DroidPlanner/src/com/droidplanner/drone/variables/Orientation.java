package com.droidplanner.drone.variables;

import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneVariable;

public class Orientation extends DroneVariable {
	private double roll = 0;
	private double pitch = 0;
	private double yaw = 0;

	public Orientation(Drone myDrone) {
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
		myDrone.onOrientationUpdate();
		notifyNewOrientationData();
	}

	private void notifyNewOrientationData() {
		if (myDrone.tuningDataListner != null) {
			myDrone.tuningDataListner.onNewOrientationData();
		}
	}

}