package com.droidplanner.fragments;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GuidedMarker {
	public Marker guidedMarker;
	private GoogleMap myMap;

	public GuidedMarker(GoogleMap myMap) {
		this.myMap = myMap;
	}

	void updateGuidedMarker(LatLng point) {
		if (guidedMarker == null) {
			addMarker(point);
		} else {
			updateMarker(point);
		}
	}

	private void updateMarker(LatLng point) {
		guidedMarker.setPosition(point);
	}

	private void addMarker(LatLng point) {
		guidedMarker = myMap.addMarker(new MarkerOptions()
				.anchor((float) 0.5, (float) 0.5)
				.position(point)
				.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
	}
}