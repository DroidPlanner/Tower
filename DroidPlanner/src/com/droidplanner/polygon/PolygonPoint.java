package com.droidplanner.polygon;

import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.droidplanner.fragments.markers.PolygonMarker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class PolygonPoint implements MarkerSource {

	public LatLng coord;

	public PolygonPoint(Double lat, Double lng) {
		coord = new LatLng(lat, lng);
	}

	public PolygonPoint(LatLng coord) {
		this.coord= coord;
	}

	@Override
	public MarkerOptions build() {
		return PolygonMarker.build(this);
	}

	@Override
	public void update(Marker marker) {
		PolygonMarker.update(marker, this);

	}
}
