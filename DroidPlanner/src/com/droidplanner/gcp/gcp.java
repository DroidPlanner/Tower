package com.droidplanner.gcp;

import com.google.android.gms.maps.model.LatLng;

public class gcp {
	public LatLng coord;
	public boolean isMarked;

	public gcp(double lat, double lng) {
		this.coord = new LatLng(lat, lng);
		this.isMarked = false;
	}

	public void toogleState() {
		isMarked = !isMarked;
	}
}