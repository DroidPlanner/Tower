package org.droidplanner.android.gcs.follow;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.helpers.units.Length;

import android.location.Location;

public abstract class FollowAlgorithm {
	public abstract void processNewLocation(Location location);

	public abstract FollowModes getType();

	protected Drone drone;
	protected Length radius;

	public FollowAlgorithm(Drone drone, Length radius) {
		super();
		this.drone = drone;
		this.radius = radius;
	}

	public void changeRadius(Double increment) {
		radius = new Length(radius.valueInMeters() + increment);
		if (radius.valueInMeters() < 0)
			radius = new Length(0);
	}

	public enum FollowModes {
		LEASH("Leash"), LEAD("Lead"), WAKEBOARD("Wakeboard"), CIRCLE("Circle"), RIGHT("Right"), LEFT(
				"Left"), ABOVE("Above");

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

		public FollowAlgorithm getAlgorithmType(Drone drone) {
			switch (this) {
			case LEASH:
				return new FollowLeash(drone, new Length(8.0));
			case WAKEBOARD:
				return new FollowWakeboard(drone, new Length(10.0));
			case RIGHT:
				return new FollowRight(drone, new Length(10.0));
			case LEFT:
				return new FollowLeft(drone, new Length(10.0));
			case CIRCLE:
				return new FollowCircle(drone, new Length(15.0), 10.0);
			case LEAD:
				return new FollowLead(drone, new Length(15.0));
			case ABOVE:
				return new FollowAbove(drone, new Length(0.0));
			}
			return null; // Should never reach this
		}
	}

}
