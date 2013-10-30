package com.droidplanner.drone.variables.mission.waypoints;

import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.google.android.gms.maps.model.LatLng;

public class LoiterInfinite extends Loiter implements MarkerSource {

	public LoiterInfinite(LatLng coord, double altitude) {
		super(coord, altitude);
	}
	
	
}