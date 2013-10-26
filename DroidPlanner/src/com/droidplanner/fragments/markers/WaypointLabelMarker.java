package com.droidplanner.fragments.markers;

import android.content.Context;
import android.graphics.Color;
import com.droidplanner.R;
import com.droidplanner.drone.variables.WaypointLabel;
import com.droidplanner.fragments.markers.helpers.MarkerWithText;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class WaypointLabelMarker {

	public static MarkerOptions build(WaypointLabel label, Context context) {
		return new MarkerOptions()
				.position(label.getCoord())
				.visible(label.showOnMap())
				.draggable(false)
				.icon(getIcon(label, context));
	}

	public static void update(Marker marker, WaypointLabel label, Context context) {
		marker.setPosition(label.getCoord());
		marker.setIcon(getIcon(label,context));
	}

	private static BitmapDescriptor getIcon(WaypointLabel label, Context context) {
        return BitmapDescriptorFactory.fromBitmap(MarkerWithText.getMarkerWithTextAndDetail(
                Color.GRAY,
                Integer.toString(label.getNumber()),
                String.format("%.0fm", label.getHeight()),
                context));
	}

}
