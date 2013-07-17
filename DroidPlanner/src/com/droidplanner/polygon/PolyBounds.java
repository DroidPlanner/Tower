package com.droidplanner.polygon;

import java.util.List;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * 
 * Object for holding boundary for a polygon
 * 
 */
class PolyBounds {
	public LatLng sw;
	public LatLng ne;

	public PolyBounds(List<LatLng> points) {
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		for (LatLng point : points) {
			builder.include(point);
		}
		LatLngBounds bounds = builder.build();
		sw = bounds.southwest;
		ne = bounds.northeast;
	}

	public double getDiag() {
		return GeoTools.latToMeters(GeoTools.getDistance(ne, sw));
	}

	public LatLng getMiddle() {
		return (new LatLng((ne.latitude + sw.latitude) / 2,
				(ne.longitude + sw.longitude) / 2));

	}
}
