package com.droidplanner.drone.variables.mission;

import java.util.List;

import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.google.android.gms.maps.model.LatLng;

public abstract class MissionCMD extends MissionItem{
	
	@Override
	public List<LatLng> getPath() throws Exception {
		throw new Exception();
	}

	@Override
	public List<MarkerSource> getMarkers() throws Exception {
		throw new Exception();
	}
	
}