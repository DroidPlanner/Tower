package com.droidplanner.drone.variables;

import com.google.android.gms.maps.model.LatLng;

public class waypoint {	
	
	private LatLng coord;
	private Double Height;

	public waypoint(LatLng c, Double h) {
		setCoord(c);
		setHeight(h);
	}

	public waypoint(Double Lat, Double Lng, Double h) {
		setCoord(new LatLng(Lat, Lng));
		setHeight(h);
	}

	public Double getHeight() {
		return Height;
	}

	public void setHeight(Double height) {
		Height = height;
	}

	public LatLng getCoord() {
		return coord;
	}

	public void setCoord(LatLng coord) {
		this.coord = coord;
	}
}