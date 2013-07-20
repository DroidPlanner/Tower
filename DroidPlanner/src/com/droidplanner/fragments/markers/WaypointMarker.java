package com.droidplanner.fragments.markers;

import java.util.Locale;

import com.droidplanner.drone.variables.waypoint;
import com.google.android.gms.maps.model.MarkerOptions;

public class WaypointMarker {
	//private Marker waypointMarker;

	public static MarkerOptions generateWapointMarker(int i, waypoint point) {
		return new MarkerOptions()
				.position(point.getCoord())
				.draggable(true)
				.title("WP" + Integer.toString(i))
				.snippet(
						String.format(Locale.ENGLISH, "%.2f",
								point.getHeight()));
	}

}
