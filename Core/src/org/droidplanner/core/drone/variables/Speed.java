package org.droidplanner.core.drone.variables;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneVariable;

public class Speed extends DroneVariable {
	private double verticalSpeed = 0;
	private double groundSpeed = 0;
	private double airSpeed = 0;
	private double targetSpeed = 0;

	public Speed(Drone myDrone) {
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