package com.droidplanner.fragments.markers;


import android.content.Context;

import com.droidplanner.R;
import com.droidplanner.drone.variables.GuidedPoint;
import com.droidplanner.fragments.markers.helpers.MarkerWithText;
import com.droidplanner.helpers.units.Altitude;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GuidedMarker {
	public static MarkerOptions build(GuidedPoint guidedPoint, Altitude altitude, Context context) {
		return new MarkerOptions()
				.position(guidedPoint.getCoord())
				.icon(getIcon(guidedPoint, altitude, context))
				.anchor(0.5f, 0.5f);
	}

	public static void update(Marker marker, GuidedPoint guidedPoint, Altitude altitude, Context context) {
		if (guidedPoint.isValid()) {			
			marker.setPosition(guidedPoint.getCoord());
			marker.setIcon(getIcon(guidedPoint, altitude, context));
			marker.setVisible(true);
		}else{
			marker.setVisible(false);
		}
	}

	private static BitmapDescriptor getIcon(GuidedPoint guidedPoint, Altitude altitude, Context context)
	{
		return BitmapDescriptorFactory.fromBitmap(MarkerWithText.getMarkerWithTextAndDetail(R.drawable.ic_wp_map_on,
			"Guided",  "", context));
	}
}