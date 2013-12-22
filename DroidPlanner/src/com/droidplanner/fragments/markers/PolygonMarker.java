package com.droidplanner.fragments.markers;

import com.droidplanner.polygon.PolygonPoint;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class PolygonMarker {

	public static MarkerOptions build(PolygonPoint wp) {
		return new MarkerOptions()
				.position(wp.coord)
				.draggable(true)
				.title("Poly")
				// TODO fix constant
				.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
	}

	public static void update(Marker marker, PolygonPoint wp) {
		marker.setPosition(wp.coord);
		marker.setTitle("Poly");// TODO fix constant
		marker.setIcon(BitmapDescriptorFactory
				.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
	}

}
