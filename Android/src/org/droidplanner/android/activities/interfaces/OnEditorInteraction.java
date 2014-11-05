package org.droidplanner.android.activities.interfaces;

import com.ox3dr.services.android.lib.coordinate.LatLong;

import org.droidplanner.android.proxy.mission.item.MissionItemProxy;

public interface OnEditorInteraction {
	public boolean onItemLongClick(MissionItemProxy item);

	public void onItemClick(MissionItemProxy item, boolean zoomToFit);

	public void onMapClick(LatLong coord);

	public void onListVisibilityChanged();
}
