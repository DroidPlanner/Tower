package com.droidplanner.helpers.geoTools;

import java.util.List;

import com.google.android.gms.maps.model.LatLng;

public class PolylineTools {

	/**
	 * 	Total length of the polyline in meters
	 * @param points
	 * @return
	 */
	public static double getPolylineLength(List<LatLng> points) {
		double lenght = 0;
		for (int i = 1; i < points.size(); i++) {
			lenght+=GeoTools.getDistance(points.get(i),points.get(i-1));
		}
		return lenght;
	}

}
