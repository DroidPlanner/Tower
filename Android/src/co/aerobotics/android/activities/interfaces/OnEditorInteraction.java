package co.aerobotics.android.activities.interfaces;

import co.aerobotics.android.proxy.mission.item.MissionItemProxy;
import com.o3dr.services.android.lib.coordinate.LatLong;

public interface OnEditorInteraction {
	void onItemClick(MissionItemProxy item, boolean zoomToFit);

	void onMapClick(LatLong coord);

	void onListVisibilityChanged();
}
