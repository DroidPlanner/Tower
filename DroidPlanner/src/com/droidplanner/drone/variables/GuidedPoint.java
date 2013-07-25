package com.droidplanner.drone.variables;

import com.droidplanner.fragments.markers.GuidedMarker;
import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GuidedPoint implements MarkerSource {
	private LatLng coord;

	public GuidedPoint(LatLng coord) {
		this.coord = coord;
	}

	@Override
	public MarkerOptions build() {
		return GuidedMarker.build(this);
	}

	@Override
	public void update(Marker markerFromGcp) {
		GuidedMarker.update(markerFromGcp, this);
	}

	public LatLng getCoord() {
		return coord;
	}
}