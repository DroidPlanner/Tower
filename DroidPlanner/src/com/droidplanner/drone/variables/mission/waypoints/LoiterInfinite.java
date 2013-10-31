package com.droidplanner.drone.variables.mission.waypoints;

import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.droidplanner.fragments.mission.MissionFragment;
import com.droidplanner.fragments.mission.MissionLoiterFragment;
import com.google.android.gms.maps.model.LatLng;

public class LoiterInfinite extends Loiter implements MarkerSource {

	public LoiterInfinite(LatLng coord, double altitude) {
		super(coord, altitude);
	}

	@Override
	public MissionFragment getDialog() {
		return new MissionLoiterFragment();
	}	
}