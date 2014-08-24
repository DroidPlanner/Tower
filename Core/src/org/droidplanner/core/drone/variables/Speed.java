package org.droidplanner.core.drone.variables;

import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.model.Drone;

public class Speed extends DroneVariable {
	public static final int COLLISION_SECONDS_BEFORE_COLLISION = 2;
	public static final double COLLISION_DANGEROUS_SPEED = -3.0;
	public static final double COLLISION_SAFE_ALTITUDE = 1.0;
	private org.droidplanner.core.helpers.units.Speed verticalSpeed = new org.droidplanner.core.helpers.units.Speed(
			0);
	private org.droidplanner.core.helpers.units.Speed groundSpeed = new org.droidplanner.core.helpers.units.Speed(
			0);
	private org.droidplanner.core.helpers.units.Speed airSpeed = new org.droidplanner.core.helpers.units.Speed(
			0);
	private org.droidplanner.core.helpers.units.Speed targetSpeed = new org.droidplanner.core.helpers.units.Speed(
			0);

	public Speed(Drone myDrone) {
		super(myDrone);
	}

	public org.droidplanner.core.helpers.units.Speed getVerticalSpeed() {
		return verticalSpeed;
	}

	public org.droidplanner.core.helpers.units.Speed getGroundSpeed() {
		return groundSpeed;
	}

	public org.droidplanner.core.helpers.units.Speed getAirSpeed() {
		return airSpeed;
	}

	public org.droidplanner.core.helpers.units.Speed getTargetSpeed() {
		return targetSpeed;
	}

	public void setSpeedError(double aspd_error) {
		targetSpeed = new org.droidplanner.core.helpers.units.Speed(aspd_error
				+ airSpeed.valueInMetersPerSecond());
	}

	public void setGroundAndAirSpeeds(double groundSpeed, double airSpeed, double climb) {
		this.groundSpeed = new org.droidplanner.core.helpers.units.Speed(groundSpeed);
		this.airSpeed = new org.droidplanner.core.helpers.units.Speed(airSpeed);
		this.verticalSpeed = new org.droidplanner.core.helpers.units.Speed(climb);
		//if drone will crash in 2 seconds at constant climb rate and climb rate < -3 m/s and altitude > 1 meter
		double altitude = myDrone.getAltitude().getAltitude();
		if(altitude + climb* COLLISION_SECONDS_BEFORE_COLLISION < 0 && climb < COLLISION_DANGEROUS_SPEED && altitude > COLLISION_SAFE_ALTITUDE){
			myDrone.getState().setCollisionImminent(true);
		}else{
			myDrone.getState().setCollisionImminent(false);
		}
	}
}
