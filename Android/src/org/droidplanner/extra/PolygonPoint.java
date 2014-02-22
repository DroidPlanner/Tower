package org.droidplanner.extra;

import org.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import org.droidplanner.fragments.markers.PolygonMarker;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class PolygonPoint implements MarkerSource {

	public LatLng coord;

	public PolygonPoint(Double lat, Double lng) {
		coord = new LatLng(lat, lng);
	}

	public PolygonPoint(LatLng coord) {
		this.coord = coord;
	}

	@Override
	public MarkerOptions build(Context context) {
		return PolygonMarker.build(this);
	}

	@Override
	public void update(Marker marker, Context context) {
		PolygonMarker.update(marker, this);

	}
}
