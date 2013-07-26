package com.droidplanner.fragments.markers;

import java.util.Locale;

import com.droidplanner.drone.variables.waypoint;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
								.getName(), wp.getHeight())).icon(getIcon(wp));
	}

	public static void update(Marker marker, waypoint wp) {
		marker.setPosition(wp.getCoord());
		marker.setTitle("WP" + Integer.toString(wp.getNumber()));
		marker.setSnippet(String.format(Locale.ENGLISH, "%s %.1fm", wp.getCmd()
				.getName(), wp.getHeight()));
		marker.setIcon(getIcon(wp));
	}

	private static BitmapDescriptor getIcon(waypoint wp) {
		switch (wp.getCmd()) {
		default:
		case CMD_NAV_WAYPOINT:
			return BitmapDescriptorFactory
					.defaultMarker(BitmapDescriptorFactory.HUE_RED);
		case CMD_NAV_LOITER_TIME:
		case CMD_NAV_LOITER_TURNS:
		case CMD_NAV_LOITER_UNLIM:
			return BitmapDescriptorFactory
					.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
		case CMD_NAV_RETURN_TO_LAUNCH:
			return BitmapDescriptorFactory
					.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
		case CMD_NAV_LAND:
			return BitmapDescriptorFactory
					.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
		case CMD_NAV_TAKEOFF:
			return BitmapDescriptorFactory
					.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA);
		case CMD_NAV_PATHPLANNING:
		case CMD_NAV_ROI:
			return BitmapDescriptorFactory
					.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
		}

	}

}
