package com.droidplanner.fragments.markers;

import com.droidplanner.drone.variables.GuidedPoint;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GuidedMarker {
	public static MarkerOptions build(GuidedPoint guidedPoint) {
		return new MarkerOptions()
				.anchor((float) 0.5, (float) 0.5)
				.position(guidedPoint.getCoord())
				.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
	}

	public static void update(Marker marker, GuidedPoint guidedPoint) {
		marker.setPosition(guidedPoint.getCoord());
	}
}