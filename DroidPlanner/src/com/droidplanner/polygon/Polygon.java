package com.droidplanner.polygon;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;

public class Polygon {

	private List<PolygonPoint> points;

	public Polygon() {
		setWaypoints(new ArrayList<PolygonPoint>());
	}

	private void setWaypoints(ArrayList<PolygonPoint> arrayList) {
		points = arrayList;
	}

	public void addPoints(List<LatLng> pointList) {
		for (LatLng point : pointList) {
			addPoint(point);
		}
	}
	
	public void addWaypoint(Double Lat, Double Lng) {
		points.add(new PolygonPoint(Lat, Lng));
	}

	public void addWaypoint(LatLng coord) {
		points.add(GeoTools.findClosestPair(coord, getLatLng()),
				new PolygonPoint(coord));
	}
	
	public void addPoint(LatLng coord) {
		points.add(new PolygonPoint(coord));
	}

	public void clearPolygon() {
		points.clear();
	}

	public List<LatLng> getLatLng() {
		List<LatLng> list = new ArrayList<LatLng>();
		for (PolygonPoint point : points) {
			list.add(point.coord);
		}
		return list;
	}

	/*
	 * A valid polygon must have at least 3 points
	 */
	public boolean isValid() {
		if (points.size() > 2)
			return true;
		else
			return false;
	}

	public List<PolygonPoint> getPolygonPoints() {
		return points;
	}

	public void movePoint(LatLng coord, int number) {
		points.get(number).coord = coord;

	}

}
