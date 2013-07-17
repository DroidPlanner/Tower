package com.droidplanner.polygon;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;

public class Polygon {

	private List<LatLng> waypoints;

	public Polygon() {
		setWaypoints(new ArrayList<LatLng>());
	}

	private void setWaypoints(ArrayList<LatLng> arrayList) {
		waypoints = arrayList;
	}

	public void addWaypoint(Double Lat, Double Lng) {
		getWaypoints().add(new LatLng(Lat, Lng));
	}

	public void addWaypoint(LatLng coord) {
		getWaypoints().add(GeoTools.findClosestPair(coord, getWaypoints()), coord);
	}

	public void clearPolygon() {
		getWaypoints().clear();
	}

	public boolean isValid() {
		if(getWaypoints().size()>2)	// A valid polygon must have at least 3 points
			return true;
		else
			return false;
	}

	public List<LatLng> getWaypoints() {
		return waypoints;
	}

	public void movePoint(LatLng coord, int number) {
		waypoints.set(number, coord);
		
	}
}
