package com.droidplanner.fragments.markers;

import java.util.Locale;

import com.droidplanner.drone.variables.waypoint;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class WaypointMarker {

	public static MarkerOptions build(waypoint wp) {
		return new MarkerOptions()
				.position(wp.getCoord())
				.draggable(true)
				.title("WP" + Integer.toString(wp.getNumber()))
				.snippet(
						String.format(Locale.ENGLISH, "%s %.1fm", wp.getCmd()
								.getName(), wp.getHeight()));
	}

	public static void update(Marker marker, waypoint wp) {
		marker.setPosition(wp.getCoord());
		marker.setTitle("WP" + Integer.toString(wp.getNumber()));
		marker.setSnippet(String.format(Locale.ENGLISH, "%s %.1fm", wp.getCmd()
				.getName(), wp.getHeight()));
	}

}
