package org.droidplanner.android.gcs.follow;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.geoTools.GeoTools;
import org.droidplanner.core.helpers.units.Length;

import android.location.Location;

public class FollowHeading extends FollowAlgorithm {

	public FollowHeading(Drone drone, Length radius, double mIN_TIME_MS) {
		super(drone, radius, mIN_TIME_MS);
	}

	@Override
	public FollowModes getType() {
		return FollowModes.HEADING;
	}

	@Override
	public void processNewLocation(Location location) {

		Coord2D gcsCoord = new Coord2D(location.getLatitude(),
				location.getLongitude());
		float bearing = location.getBearing();

		Coord2D goCoord = GeoTools.newCoordFromBearingAndDistance(gcsCoord,
				bearing + 90.0, radius.valueInMeters());
		drone.guidedPoint.newGuidedCoord(goCoord);
	}

}
