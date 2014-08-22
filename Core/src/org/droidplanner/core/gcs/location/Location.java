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

	public Location(Coord2D coord2d) {
		coordinate = coord2d;
	}

	public Location(Coord2D coord2d, double accuracy) {
		coordinate = coord2d;
		this.accuracy = accuracy;
	}

	public Coord2D getCoord() {
		return coordinate;
	}

	public double getAccuracy() {
		return accuracy;
	}

}
