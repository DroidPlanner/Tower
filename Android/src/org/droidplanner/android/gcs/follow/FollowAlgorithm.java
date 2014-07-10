package org.droidplanner.android.gcs.follow;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.helpers.units.Length;

import android.location.Location;

public abstract class FollowAlgorithm {
	public abstract void processNewLocation(Location location);

	public abstract FollowModes getType();

	protected Drone drone;
	protected Length radius;
	protected double MIN_TIME_MS;

	public FollowAlgorithm(Drone drone, Length radius, double mIN_TIME_MS) {
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

	public enum FollowModes {
		LEASH("Leash"), LEAD("Lead"), WAKEBOARD("Wakeboard"), CIRCLE(
				"Circle");

		private String name;

		FollowModes(String str) {
			name = str;
		}

		@Override
		public String toString() {
			return name;
		}

		public FollowModes next() {
			return values()[(ordinal() + 1) % values().length];
		}

		public FollowAlgorithm getAlgorithmType(Drone drone, Length radius,
				double mIN_TIME_MS) {
			switch (this) {
			default:
			case LEASH:
				return new FollowLeash(drone, radius, mIN_TIME_MS);
			case CIRCLE:
				return new FollowCircle(drone, radius, mIN_TIME_MS);
			case LEAD:
				return new FollowLead(drone, radius, mIN_TIME_MS);
			case WAKEBOARD:
				return new FollowWakeboard(drone, radius, mIN_TIME_MS);
			}
		}
	}

}
