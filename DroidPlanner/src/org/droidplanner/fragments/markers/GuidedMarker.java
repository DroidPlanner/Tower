package org.droidplanner.fragments.markers;


import org.droidplanner.drone.variables.GuidedPoint;
import org.droidplanner.fragments.markers.helpers.MarkerWithText;
import org.droidplanner.helpers.units.Altitude;

import android.content.Context;

import org.droidplanner.R;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GuidedMarker {
	public static MarkerOptions build(GuidedPoint guidedPoint, Altitude altitude, Context context) {
		return new MarkerOptions()
				.position(guidedPoint.getCoord())
				.icon(getIcon(guidedPoint, altitude, context))
				.anchor(0.5f, 0.5f)
				.visible(false);
	}

	public static void update(Marker marker, GuidedPoint guidedPoint, Altitude altitude, Context context) {
		if (guidedPoint.isActive()) {			
			marker.setPosition(guidedPoint.getCoord());
			marker.setIcon(getIcon(guidedPoint, altitude, context));
			marker.setVisible(true);
		}else{
			marker.setVisible(false);
		}
	}

	private static BitmapDescriptor getIcon(GuidedPoint guidedPoint, Altitude altitude, Context context)
	{
		return BitmapDescriptorFactory.fromBitmap(MarkerWithText.getMarkerWithTextAndDetail(R.drawable.ic_wp_map,
			"Guided",  "", context));
	}
}