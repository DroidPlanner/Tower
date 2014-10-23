package org.droidplanner.android.activities.interfaces;

import org.droidplanner.android.proxy.mission.item.MissionItemProxy;
import org.droidplanner.core.helpers.coordinates.Coord2D;

public interface OnEditorInteraction {
	public boolean onItemLongClick(MissionItemProxy item);

	public void onItemClick(MissionItemProxy item, boolean zoomToFit);

	public void onMapClick(Coord2D coord);

	public void onListVisibilityChanged();
}
