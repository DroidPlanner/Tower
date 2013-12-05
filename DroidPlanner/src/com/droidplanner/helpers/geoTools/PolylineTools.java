package com.droidplanner.helpers.geoTools;

import java.util.List;

import com.droidplanner.helpers.units.Length;
import com.google.android.gms.maps.model.LatLng;

public class PolylineTools {

	/**
	 * 	Total length of the polyline in meters
	 * @param points
	 * @return
	 */
	public static Length getPolylineLength(List<LatLng> points) {
		double lenght = 0;
		for (int i = 1; i < points.size(); i++) {
			lenght+=GeoTools.getDistance(points.get(i),points.get(i-1)).valueInMeters();
		}
		return new Length(lenght);
	}

}
