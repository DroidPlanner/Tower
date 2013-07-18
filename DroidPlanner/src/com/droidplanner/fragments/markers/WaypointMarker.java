package com.droidplanner.fragments.markers;

import java.util.Locale;

import com.MAVLink.waypoint;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class WaypointMarker {
	public Marker waypointMarker;

	public static MarkerOptions generateWapointMarker(int i, waypoint point) {
		return new MarkerOptions()
				.position(point.coord)
				.draggable(true)
				.title("WP" + Integer.toString(i))
				.snippet(
						String.format(Locale.ENGLISH, "%.2f",
								point.Height));
	}

}
