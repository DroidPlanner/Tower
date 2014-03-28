package org.droidplanner.android.activities.interfaces;

import org.droidplanner.core.mission.MissionItem;

import com.google.android.gms.maps.model.LatLng;

public interface OnEditorInteraction {
		public boolean onItemLongClick(MissionItem item);
		public void onItemClick(MissionItem item);
		public void onMapClick(LatLng coord);
		public void onListVisibilityChanged();
}
