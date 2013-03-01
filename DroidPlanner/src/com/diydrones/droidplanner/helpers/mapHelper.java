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

	public static void addGcpMarkerToMap(GoogleMap mMap, int i, LatLng coord,
			boolean isChecked) {
		if (isChecked) {
			mMap.addMarker(new MarkerOptions()
					.position(coord)
					.title(String.valueOf(i))
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.placemark_circle_blue))
					.anchor((float) 0.5, (float) 0.5));
		} else {
			mMap.addMarker(new MarkerOptions()
					.position(coord)
					.title(String.valueOf(i))
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.placemark_circle_red))
					.anchor((float) 0.5, (float) 0.5));
		}
	}

}
