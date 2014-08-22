package org.droidplanner.core.drone.variables;

import org.droidplanner.core.model.Drone;
import org.droidplanner.core.drone.DroneVariable;

public class Speed extends DroneVariable {
	private org.droidplanner.core.helpers.units.Speed verticalSpeed = new org.droidplanner.core.helpers.units.Speed(0);;
	private org.droidplanner.core.helpers.units.Speed groundSpeed = new org.droidplanner.core.helpers.units.Speed(0);;
	private org.droidplanner.core.helpers.units.Speed airSpeed = new org.droidplanner.core.helpers.units.Speed(0);;
	private org.droidplanner.core.helpers.units.Speed targetSpeed = new org.droidplanner.core.helpers.units.Speed(0);

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
		targetSpeed = new org.droidplanner.core.helpers.units.Speed(aspd_error + airSpeed.valueInMetersPerSecond());
	}

	public void setGroundAndAirSpeeds(double groundSpeed, double airSpeed, double climb) {
		this.groundSpeed = new org.droidplanner.core.helpers.units.Speed(groundSpeed);
		this.airSpeed = new org.droidplanner.core.helpers.units.Speed(airSpeed);
		this.verticalSpeed = new org.droidplanner.core.helpers.units.Speed(climb);
	}
}