package org.droidplanner.android.activities.interfaces;

import org.droidplanner.android.proxy.mission.item.MissionItemProxy;
import org.droidplanner.core.helpers.coordinates.Coord2D;

import com.google.android.gms.maps.model.LatLng;

public interface OnEditorInteraction {
	public boolean onItemLongClick(MissionItemProxy item);

	public void onItemClick(MissionItemProxy item);

	public void onMapClick(Coord2D coord);

	public void onListVisibilityChanged();
}
