package org.droidplanner.core.gcs.follow;

import org.droidplanner.core.gcs.location.Location;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.geoTools.GeoTools;
import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.model.Drone;

public abstract class FollowHeadingAngle extends FollowAlgorithm {

	protected double angleOffset;

	protected FollowHeadingAngle(Drone drone, Length radius, double angleOffset) {
		super(drone, radius);
		this.angleOffset = angleOffset;
	}

	@Override
	public void processNewLocation(Location location) {

		Coord2D gcsCoord = new Coord2D(location.getCoord().getLat(), location.getCoord().getLng());
		double bearing = location.getBearing();

		Coord2D goCoord = GeoTools.newCoordFromBearingAndDistance(gcsCoord, bearing + angleOffset,
				radius.valueInMeters());
		drone.getGuidedPoint().newGuidedCoord(goCoord);
	}

}