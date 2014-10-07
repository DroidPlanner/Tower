package org.droidplanner.core.gcs.location;

import org.droidplanner.core.helpers.coordinates.Coord2D;

public class Location {

	public interface LocationReceiver {
		public void onLocationChanged(Location location);
	}

	public interface LocationFinder {
		public void enableLocationUpdates();

		public void disableLocationUpdates();

		public void setLocationListner(LocationReceiver receiver);
	}

	private Coord2D coordinate;
	private double accuracy = 0.0;
	private double heading = 0.0;
	private double speed = 0.0;
	private double distanceToPrevious = 0.0;
	private long timeSinceLast = 0L;

	public Location(Coord2D coord2d) {
		coordinate = coord2d;
	}

	public Location(Coord2D coord2d, double accuracy, float heading, float speed, double distance, long since) {
		coordinate = coord2d;
		this.accuracy = accuracy;
		this.heading = heading;
		this.speed = speed;
		this.distanceToPrevious = distance;
		this.timeSinceLast = since;
	}

	public Coord2D getCoord() {
		return coordinate;
	}

	public double getAccuracy() {
		return accuracy;
	}

	public double getBearing() {
		return heading;
	}

	public double getSpeed() {
		return speed;
	}

	public double getDistanceToPrevious() {
		return distanceToPrevious;
	}

	public long getTimeSinceLast() {
		return timeSinceLast;
	}

	public boolean hasTimeSinceLast() {
		return (timeSinceLast > 0);
	}

	public boolean hasDistanceToPrevious() {
		return (distanceToPrevious > 0.0);
	}
}
