package org.droidplanner.core.gcs.roi;

import org.droidplanner.core.MAVLink.MavLinkROI;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.drone.DroneInterfaces.Handler;
import org.droidplanner.core.gcs.location.Location;
import org.droidplanner.core.gcs.location.Location.LocationReceiver;
import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.helpers.units.Altitude;


public class ROIEstimator implements LocationReceiver {
	private Drone drone;

	public ROIEstimator(Handler handler, Drone drone) {
		this.drone = drone;
	}

	@Override
	public void onLocationChanged(Location location) {
		MavLinkROI.setROI(drone, new Coord3D(location.getCoord(), new Altitude(0.0)));		
	}
}
