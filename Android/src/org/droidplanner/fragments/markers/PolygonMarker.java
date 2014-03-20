package org.droidplanner.fragments.markers;

import org.droidplanner.extra.DroneHelper;
import org.droidplanner.helpers.coordinates.Coord2D;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class PolygonMarker {

	public static MarkerOptions build(Coord2D coord) {
		return new MarkerOptions()
				.position(DroneHelper.CoordToLatLang(coord))
				.draggable(true)
				.title("Poly")
				// TODO fix constant
				.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
	}

	public static void update(Marker marker, Coord2D coord) {
		marker.setPosition(DroneHelper.CoordToLatLang(coord));
		marker.setTitle("Poly");// TODO fix constant
		marker.setIcon(BitmapDescriptorFactory
				.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
	}

}
