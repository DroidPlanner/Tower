package com.droidplanner.circle;

import com.droidplanner.fragments.markers.CircleMarker;
import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class CirclePoint implements MarkerSource {
	public LatLng coord;
	public boolean isTheCenter;

	public CirclePoint(LatLng point) {
		this.coord = point;
		this.isTheCenter = false;
	}

	public void setAsCenter() {
		isTheCenter = true;
	}

	@Override
	public MarkerOptions build() {
		return CircleMarker.build(this);
	}

	@Override
	public void update(Marker markerFromGcp) {
		CircleMarker.update(markerFromGcp, this);
	}
}