package org.droidplanner.core.gcs.location;

import org.droidplanner.core.helpers.coordinates.Coord2D;


public class Location{

	public interface LocationReceiver {
		public void onLocationChanged(Location location);
	}

	public interface LocationFinder {
		public void enableLocationUpdates();
		public void disableLocationUpdates();
		public void setLocationListner(LocationReceiver receiver);
	}
	
	private Coord2D coordinate;
	
	public Location(Coord2D coord2d) {
		coordinate = coord2d;
	}

	public Coord2D getCoord() {
		return coordinate;
	}
	
}