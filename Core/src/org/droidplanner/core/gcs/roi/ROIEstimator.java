package org.droidplanner.core.gcs.roi;

import org.droidplanner.core.MAVLink.MavLinkROI;
import org.droidplanner.core.drone.DroneInterfaces.Handler;
import org.droidplanner.core.gcs.location.Location;
import org.droidplanner.core.gcs.location.Location.LocationReceiver;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.helpers.geoTools.GeoTools;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.model.Drone;

/**
 * Uses location data from Android's FusedLocation LocationManager at 1Hz and
 * calculates new points at 10Hz based on Last Location and Last Velocity.
 * 
 */
public class ROIEstimator implements LocationReceiver {

	private static final int TIMEOUT = 100;
	private Location realLocation;
	private long timeOfLastLocation;

	private Drone drone;
	private Handler watchdog;
	public Runnable watchdogCallback = new Runnable() {
		@Override
		public void run() {
			updateROI();
		}

	};

	public ROIEstimator(Handler handler, Drone drone) {
		this.watchdog = handler;
		this.drone = drone;
	}

	public void disableLocationUpdates() {
		watchdog.removeCallbacks(watchdogCallback);
	}

	@Override
	public void onLocationChanged(Location location) {
		disableLocationUpdates();
		realLocation = location;
		timeOfLastLocation = System.currentTimeMillis();
		updateROI();
	}

	private void updateROI() {
		if (realLocation == null) {
			return;
		}
		Coord2D gcsCoord = new Coord2D(realLocation.getCoord().getLat(), realLocation.getCoord()
				.getLng());

		double bearing = realLocation.getBearing();
		double distanceTraveledSinceLastPoint = realLocation.getSpeed()
				* (System.currentTimeMillis() - timeOfLastLocation) / 1000f;
		Coord2D goCoord = GeoTools.newCoordFromBearingAndDistance(gcsCoord, bearing,
				distanceTraveledSinceLastPoint);
		if (distanceTraveledSinceLastPoint > 0.0) {
			MavLinkROI.setROI(drone, new Coord3D(goCoord.getLat(), goCoord.getLng(), new Altitude(
					1.0)));
		}
		watchdog.postDelayed(watchdogCallback, TIMEOUT);

	}
}
