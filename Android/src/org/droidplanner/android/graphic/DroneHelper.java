package org.droidplanner.android.graphic;

import org.droidplanner.core.helpers.coordinates.Coord2D;

import com.google.android.gms.maps.model.LatLng;

public class DroneHelper {
	static public LatLng CoordToLatLang(Coord2D coord){
		return new LatLng(coord.getLat(), coord.getLng());
	}

	public static Coord2D LatLngToCoord(LatLng point) {
		return new Coord2D(point.longitude,point.latitude);
	}
}
