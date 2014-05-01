package org.droidplanner.android.graphic;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord2D;

import com.google.android.gms.maps.model.LatLng;

public class DroneHelper {
	static public LatLng CoordToLatLang(Coord2D coord) {
		return new LatLng(coord.getLat(), coord.getLng());
	}

	public static Coord2D LatLngToCoord(LatLng point) {
		return new Coord2D(point.latitude, point.longitude);
	}

	public static List<LatLng> CoordToLatLang(
			List<Coord2D> points) {
		List<LatLng> result = new ArrayList<LatLng>();
		for (Coord2D coord2d : points) {
			result.add(CoordToLatLang(coord2d));
		}
		return result;
	}
}
