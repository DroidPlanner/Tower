package com.droidplanner.fragments.markers;

import com.droidplanner.circle.CirclePoint;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class CircleMarker {

	public static MarkerOptions build(CirclePoint point) {
		return new MarkerOptions().position(point.coord)
				.icon(getIcon(point)).anchor((float) 0.5, (float) 0.5);
	}

	public static void update(Marker marker, CirclePoint point) {
		marker.setPosition(point.coord);
		marker.setIcon(getIcon(point));
	}

	private static BitmapDescriptor getIcon(CirclePoint point) {
		if (point.isTheCenter) {
			return BitmapDescriptorFactory
					.defaultMarker(BitmapDescriptorFactory.HUE_RED);
		} else {
			return BitmapDescriptorFactory
					.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
		}

	}
}