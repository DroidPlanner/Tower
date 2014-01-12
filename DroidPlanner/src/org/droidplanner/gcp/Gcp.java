package org.droidplanner.gcp;

import org.droidplanner.fragments.markers.GcpMarker;
import org.droidplanner.fragments.markers.MarkerManager.MarkerSource;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Gcp implements MarkerSource {
	public LatLng coord;
	public boolean isMarked;

	public Gcp(double lat, double lng) {
		this.coord = new LatLng(lat, lng);
		this.isMarked = false;
	}

	public void toogleState() {
		isMarked = !isMarked;
	}

	@Override
	public MarkerOptions build(Context context) {
		return GcpMarker.build(this);
	}

	@Override
	public void update(Marker markerFromGcp, Context context) {
		GcpMarker.update(markerFromGcp, this);
	}
}