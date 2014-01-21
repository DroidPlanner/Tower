package org.droidplanner.activities.helpers;

import org.droidplanner.drone.variables.mission.MissionItem;

import com.google.android.gms.maps.model.LatLng;

public interface OnEditorInteraction {
		public boolean onItemLongClick(MissionItem item);
		public void onItemClick(MissionItem item);
		public void onMapClick(LatLng coord);
		public void onListVisibilityChanged();
}
