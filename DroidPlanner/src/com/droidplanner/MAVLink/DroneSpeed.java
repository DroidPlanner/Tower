package com.droidplanner.MAVLink;

public class DroneSpeed extends DroneVariable{
	public double verticalSpeed;
	public double groundSpeed;
	public double airSpeed;
	public double targetSpeed;

	public DroneSpeed(Drone myDrone,double verticalSpeed, double groundSpeed,
			double airSpeed, double targetSpeed) {
		super(myDrone);
		this.verticalSpeed = verticalSpeed;
		this.groundSpeed = groundSpeed;
		this.airSpeed = airSpeed;
		this.targetSpeed = targetSpeed;
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