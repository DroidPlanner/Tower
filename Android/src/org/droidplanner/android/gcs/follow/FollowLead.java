package org.droidplanner.android.gcs.follow;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.geoTools.GeoTools;
import org.droidplanner.core.helpers.units.Length;

import android.location.Location;

public class FollowLead extends FollowAlgorithm {

	public FollowLead(Drone drone, Length radius) {
		super(drone, radius);
	}

	@Override
	public FollowModes getType() {
		return FollowModes.LEAD;
	}

	@Override
	public void processNewLocation(Location location) {

		Coord2D gcsCoord = new Coord2D(location.getLatitude(),
				location.getLongitude());
		float bearing = location.getBearing();

		Coord2D goCoord = GeoTools.newCoordFromBearingAndDistance(gcsCoord,
				bearing, radius.valueInMeters());
		drone.guidedPoint.newGuidedCoord(goCoord);
	}

}
