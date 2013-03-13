package com.diydrones.droidplanner.helpers;

import com.diydrones.droidplanner.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;

public class mapHelper {

	public static void setupMapOverlay(GoogleMap mMap, boolean useOfflineMaps) {
		if (useOfflineMaps) {
			mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
			mMap.addTileOverlay(new TileOverlayOptions()
					.tileProvider(new LocalMapTileProvider()));
		} else {
			mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		}
	}
}
