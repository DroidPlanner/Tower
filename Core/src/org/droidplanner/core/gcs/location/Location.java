package org.droidplanner.core.gcs.location;

import org.droidplanner.core.helpers.coordinates.Coord2D;

public class Location {

	public interface LocationReceiver {
		public void onLocationChanged(Location location);
	}

	public interface LocationFinder {
		public void enableLocationUpdates();

		public void disableLocationUpdates();

		public void setLocationListener(LocationReceiver receiver);
	}

	private Coord2D coordinate;
	private double heading = 0.0;
	private double speed = 0.0;
	private boolean isAccurate;

	public Location(Coord2D coord2d) {
		coordinate = coord2d;
	}

	public Location(Coord2D coord2d, float heading, float speed, boolean isAccurate) {
		coordinate = coord2d;
		this.heading = heading;
		this.speed = speed;
		this.isAccurate = isAccurate;
	}

	public Coord2D getCoord() {
		return coordinate;
	}

	public boolean isAccurate() {
		return this.isAccurate;
	}

	public double getBearing() {
		return heading;
	}

	public double getSpeed() {
		return speed;
	}

}
