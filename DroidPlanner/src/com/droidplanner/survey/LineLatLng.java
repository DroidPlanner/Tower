package com.droidplanner.survey;

import com.google.android.gms.maps.model.LatLng;

public class LineLatLng {
	public LatLng p1;
	public LatLng p2;

	public LineLatLng(LatLng p1, LatLng p2) {
		this.p1 = p1;
		this.p2 = p2;
	}
}