package org.droidplanner.drone.variables.mission.commands;

import java.util.List;

import org.droidplanner.drone.variables.mission.MissionItem;
import org.droidplanner.fragments.markers.MarkerManager.MarkerSource;

import com.google.android.gms.maps.model.LatLng;

public abstract class MissionCMD extends MissionItem{
	
	public MissionCMD(MissionItem item) {
		super(item);
	}

	@Override
	public List<LatLng> getPath() throws Exception {
		throw new Exception();
	}

	@Override
	public List<MarkerSource> getMarkers() throws Exception {
		throw new Exception();
	}
	
}