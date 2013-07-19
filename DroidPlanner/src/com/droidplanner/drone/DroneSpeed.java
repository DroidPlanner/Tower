package com.droidplanner.drone;

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
	
	public void setSpeedError(double aspd_error) {
		targetSpeed = aspd_error + airSpeed;
	}
	
	public void setGroundAndAirSpeeds(double groundSpeed, double airSpeed,
			double climb) {
		this.groundSpeed = groundSpeed;
		this.airSpeed = airSpeed;
		this.verticalSpeed = climb;
	}
}