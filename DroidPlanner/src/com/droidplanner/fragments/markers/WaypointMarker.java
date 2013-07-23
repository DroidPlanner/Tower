package com.droidplanner.fragments.markers;

import java.util.Locale;

import com.droidplanner.drone.variables.waypoint;
import com.google.android.gms.maps.model.MarkerOptions;

public class WaypointMarker {

	public static MarkerOptions generateWapointMarker(waypoint wp) {
		return new MarkerOptions()
				.position(wp.getCoord())
				.draggable(true)
				.title("WP" + Integer.toString(wp.getNumber()))
				.snippet(
						String.format(Locale.ENGLISH, "%s %.1fm", wp.getCmd()
								.getName(), wp.getHeight()));
	}

}
