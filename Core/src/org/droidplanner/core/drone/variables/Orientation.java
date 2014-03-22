package org.droidplanner.core.drone.variables;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneVariable;

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
		myDrone.events.notifyDroneEvent(DroneEventsType.ATTIUTDE);
	}

}