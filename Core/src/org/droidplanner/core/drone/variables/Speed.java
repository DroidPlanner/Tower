package org.droidplanner.core.drone.variables;

import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.parameters.Parameter;

public class Speed extends DroneVariable {
	public static final int COLLISION_SECONDS_BEFORE_COLLISION = 2;
	public static final double COLLISION_DANGEROUS_SPEED_METERS_PER_SECOND = -3.0;
	public static final double COLLISION_SAFE_ALTITUDE_METERS = 1.0;
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
		checkCollisionIsImminent();
	}

	public org.droidplanner.core.helpers.units.Speed getSpeedParameter() {
		Parameter param = myDrone.getParameters().getParameter("WPNAV_SPEED");
		if (param == null) {
			return null;
		} else {
			return new org.droidplanner.core.helpers.units.Speed(param.value / 100);
		}

	}

	/**
	 * if drone will crash in 2 seconds at constant climb rate and climb rate <
	 * -3 m/s and altitude > 1 meter
	 */
	private void checkCollisionIsImminent() {

		double altitude = myDrone.getAltitude().getAltitude();
		if (altitude + verticalSpeed.valueInMetersPerSecond() * COLLISION_SECONDS_BEFORE_COLLISION < 0
				&& verticalSpeed.valueInMetersPerSecond() < COLLISION_DANGEROUS_SPEED_METERS_PER_SECOND
				&& altitude > COLLISION_SAFE_ALTITUDE_METERS) {
			myDrone.getAltitude().setCollisionImminent(true);
		} else {
			myDrone.getAltitude().setCollisionImminent(false);
		}
	}

}
