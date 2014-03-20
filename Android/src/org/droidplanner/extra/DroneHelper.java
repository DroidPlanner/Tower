package org.droidplanner.extra;

import org.droidplanner.helpers.coordinates.Coord2D;

import com.google.android.gms.maps.model.LatLng;

public class DroneHelper {
	static public LatLng CoordToLatLang(Coord2D coord){
		return new LatLng(coord.getLat(), coord.getLng());
	}
}
