package org.droidplanner.desktop.logic;

import java.util.Timer;
import java.util.TimerTask;

import org.droidplanner.core.gcs.location.Location;
import org.droidplanner.core.gcs.location.Location.LocationFinder;
import org.droidplanner.core.gcs.location.Location.LocationReceiver;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.geoTools.GeoTools;

class FakeLocation implements LocationFinder {
	private LocationReceiver receiver;

	@Override
	public void setLocationListner(LocationReceiver receiver) {
		this.receiver = receiver;				
	}

	@Override
	public void enableLocationUpdates() {
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			Coord2D start = new Coord2D(-35.363154,149.165067);
			Location[] locations = {
					new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 00)),
					new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 10)),
					new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 20)),
					new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 30)),
					new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 40)),
					new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 50)),
					new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 60)),
					new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 70)),
					new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 80)),
					new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 90)),
					new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 80)), 
					new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 70)),
					new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 60)),
					new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 50)),
					new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 40)),
					new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 30)),
					new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 20)),
					new Location(GeoTools.newCoordFromBearingAndDistance(start, 90, 10)),
					};
			int index = 0;

			@Override
			public void run() {
				receiver.onLocationChanged(locations[index]);
				index++;
				if (index >= locations.length) {
					index = 0;
				}

			}
		}, 0, 5*1000);

	}

	@Override
	public void disableLocationUpdates() {
		// TODO Auto-generated method stub

	}
}