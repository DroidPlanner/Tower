package org.droidplanner.android.activities.interfaces;

import org.droidplanner.android.mission.item.MissionItemRender;
import org.droidplanner.core.mission.MissionItem;

import com.google.android.gms.maps.model.LatLng;

public interface OnEditorInteraction {
	public boolean onItemLongClick(MissionItemRender item);

	public void onItemClick(MissionItemRender item);

	public void onMapClick(LatLng coord);

	public void onListVisibilityChanged();
}
