package org.droidplanner.core.drone.variables;

import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.model.Drone;

public class Altitude extends DroneVariable {
	private static final double FOUR_HUNDRED_FEET_IN_METERS = 121.92;
	private double altitude = 0;
	private double targetAltitude = 0;
	private double previousAltitude = 0;

	private boolean isCollisionImminent;

	public Altitude(Drone myDrone) {
		super(myDrone);
	}

	public double getAltitude() {
		return altitude;
	}

	public double getTargetAltitude() {
		return targetAltitude;
	}

	public boolean isCollisionImminent() {
		return isCollisionImminent;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
		if (altitude > FOUR_HUNDRED_FEET_IN_METERS
				&& previousAltitude <= FOUR_HUNDRED_FEET_IN_METERS) {
			myDrone.notifyDroneEvent(DroneInterfaces.DroneEventsType.WARNING_400FT_EXCEEDED);
		}
		previousAltitude = altitude;
	}

	public void setAltitudeError(double alt_error) {
		targetAltitude = alt_error + altitude;
	}

	public void setCollisionImminent(boolean isCollisionImminent) {
		if (this.isCollisionImminent != isCollisionImminent) {
			this.isCollisionImminent = isCollisionImminent;
			myDrone.notifyDroneEvent(DroneInterfaces.DroneEventsType.STATE);
		}
	}

}