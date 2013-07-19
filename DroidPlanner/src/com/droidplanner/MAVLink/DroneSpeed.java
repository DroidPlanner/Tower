package com.droidplanner.MAVLink;

public class DroneSpeed extends DroneVariable {
	public double verticalSpeed = 0;
	public double groundSpeed = 0;
	public double airSpeed = 0;
	public double targetSpeed = 0;

	public DroneSpeed(Drone myDrone) {
		super(myDrone);
	}

	public double getVerticalSpeed() {
		return verticalSpeed;
	}

	public double getGroundSpeed() {
		return groundSpeed;
	}

	public double getAirSpeed() {
		return airSpeed;
	}

	public double getTargetSpeed() {
		return targetSpeed;
	}
}