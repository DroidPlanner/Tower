package com.diydrones.droidplanner;

import com.google.android.gms.maps.model.LatLng;

public class waypoint {
	public LatLng coord;
	public Double Height;

	public waypoint(LatLng c, Double h) {
		coord = c;
		Height = h;
	}

	public waypoint(Double Lat, Double Lng, Double h) {
		coord = new LatLng(Lat, Lng);
		Height = h;
	}
}