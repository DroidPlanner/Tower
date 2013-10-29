package com.droidplanner.drone.variables.mission.waypoints;

import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.google.android.gms.maps.model.LatLng;

public class Waypoint extends GenericWaypoint implements MarkerSource {

	public Waypoint(LatLng coord, double altitude) {
		super(coord, altitude);
	}
	
}