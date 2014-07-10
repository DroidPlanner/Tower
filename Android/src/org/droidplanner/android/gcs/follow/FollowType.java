package org.droidplanner.android.gcs.follow;

import org.droidplanner.android.gcs.follow.Follow.FollowModes;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.helpers.units.Length;

import android.location.Location;

public abstract class FollowType {
	public abstract void processNewLocation(Location location);
	public abstract FollowModes getType();

	protected Drone drone;
	protected Length radius;
	protected double MIN_TIME_MS;

	public FollowType(Drone drone, Length radius, double mIN_TIME_MS) {
		super();
		this.drone = drone;
		this.radius = radius;
		MIN_TIME_MS = mIN_TIME_MS;
	}

	public void changeRadius(Double increment) {
		radius = new Length(radius.valueInMeters() + increment);
		if (radius.valueInMeters() < 0)
			radius = new Length(0);
	}

}
