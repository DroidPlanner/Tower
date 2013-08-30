package com.droidplanner.polygon;

import java.util.ArrayList;
import java.util.List;

import com.droidplanner.fragments.helpers.MapPath.PathSource;
import com.droidplanner.helpers.geoTools.GeoTools;
import com.droidplanner.helpers.geoTools.LineLatLng;
import com.google.android.gms.maps.model.LatLng;

public class Polygon implements PathSource {

	private List<PolygonPoint> points = new ArrayList<PolygonPoint>();

	public void addPoints(List<LatLng> pointList) {
		for (LatLng point : pointList) {
			addPoint(point);
		}
	}

	public void addPoint(LatLng coord) {
		points.add(new PolygonPoint(coord));
	}

	public void clearPolygon() {
		points.clear();
	}

	public List<LatLng> getLatLngList() {
		List<LatLng> list = new ArrayList<LatLng>();
		for (PolygonPoint point : points) {
			list.add(point.coord);
		}
		return list;
	}

	public List<LineLatLng> getLines() {
		List<LineLatLng> list = new ArrayList<LineLatLng>();
		for (int i = 0; i < points.size(); i++) {
			int endIndex = (i==0)? points.size()-1: i-1;
			list.add(new LineLatLng(points.get(i).coord,points.get(endIndex).coord));
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

	public Double getArea() {
		return GeoTools.getArea(this);
	}

	@Override
	public List<LatLng> getPathPoints() {
		List<LatLng> path = getLatLngList();
		if (getLatLngList().size() > 2) {
			path.add(path.get(0));
		}
		return path;
	}

}
