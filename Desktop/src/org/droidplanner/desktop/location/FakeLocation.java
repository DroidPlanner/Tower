package org.droidplanner.desktop.location;

import java.util.Timer;
import java.util.TimerTask;

import org.droidplanner.core.gcs.location.Location;
import org.droidplanner.core.gcs.location.Location.LocationFinder;
import org.droidplanner.core.gcs.location.Location.LocationReceiver;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.geoTools.GeoTools;

public class FakeLocation implements LocationFinder {
	private static final int UPDATE_INTERVAL = 500;
	private static final double SPEED = 3;
	private LocationReceiver receiver;

	@Override
	public void setLocationListener(LocationReceiver receiver) {
		this.receiver = receiver;
	}

	@Override
	public void enableLocationUpdates() {
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			Coord2D loc = new Coord2D(-35.363154, 149.165067);
			long lastTime = System.currentTimeMillis();

			@Override
			public void run() {
				long now = System.currentTimeMillis();
				double dt = (now - lastTime) / 1000.0;
				lastTime = now;
				if (((now / (90 * 1000)) % 2) == 0) {
					loc = GeoTools.newCoordFromBearingAndDistance(loc, 90, SPEED * dt);
				} else {
					loc = GeoTools.newCoordFromBearingAndDistance(loc, 90, -SPEED * dt);
				}
				receiver.onLocationChanged(new Location(loc));
			}
		}, 0, UPDATE_INTERVAL);

	}

	@Override
	public void disableLocationUpdates() {
		// TODO Auto-generated method stub

	}

	
}