package com.droidplanner.drone.variables.mission;

import java.util.List;

import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.droidplanner.fragments.mission.MissionFragment;
import com.google.android.gms.maps.model.LatLng;

public abstract class MissionItem {

	/**
	 * Gets a flight path for this item
	 * @return the path as a list
	 * @throws Exception if path not available
	 */
	public abstract List<LatLng> getPath() throws Exception;

	/**
	 * Gets all markers for this item
	 * @return list of markers
	 * @throws Exception if this item doesn't have markers
	 */
	public abstract List<MarkerSource> getMarkers() throws Exception;

	/**
	 * Return a new dialog for this MissionItem
	 * @return
	 */
	public abstract MissionFragment getDialog(); 
	
}