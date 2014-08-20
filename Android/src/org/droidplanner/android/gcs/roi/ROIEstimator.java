package org.droidplanner.android.gcs.roi;

import org.droidplanner.android.gcs.location.LocationReceiver;
import org.droidplanner.core.MAVLink.MavLinkROI;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.drone.DroneInterfaces.Handler;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.helpers.units.Altitude;

import android.location.Location;

public class ROIEstimator implements LocationReceiver {
	private Drone drone;

	public ROIEstimator(Handler handler, Drone drone) {
		this.drone = drone;
	}

	public void disableLocationUpdates() {
	}

	@Override
	public void onLocationChanged(Location location) {
		Coord2D gcsCoord = new Coord2D(location.getLatitude(),
				location.getLongitude());
		MavLinkROI.setROI(drone, new Coord3D(gcsCoord, new Altitude(0.0)));		
	}
}
