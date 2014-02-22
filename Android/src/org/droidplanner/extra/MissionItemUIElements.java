package org.droidplanner.extra;

import java.util.List;

import org.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import org.droidplanner.fragments.mission.MissionDetailFragment;

import com.google.android.gms.maps.model.LatLng;

public interface MissionItemUIElements {

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
	 * Return a new detail Fragment for this MissionItem
	 * @return
	 */
	public abstract MissionDetailFragment getDetailFragment();

}