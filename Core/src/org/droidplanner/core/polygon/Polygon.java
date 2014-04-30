package org.droidplanner.core.polygon;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.geoTools.GeoTools;
import org.droidplanner.core.helpers.geoTools.LineLatLng;
import org.droidplanner.core.helpers.units.Area;

public class Polygon {

	private List<Coord2D> points = new ArrayList<Coord2D>();

	public void addPoints(List<Coord2D> pointList) {
		for (Coord2D point : pointList) {
			addPoint(point);
		}
	}

	public void addPoint(Coord2D coord) {
		points.add(coord);
	}

	public void clearPolygon() {
		points.clear();
	}

	public List<Coord2D> getPoints() {
		return points;
	}

	public List<LineLatLng> getLines() {
		List<LineLatLng> list = new ArrayList<LineLatLng>();
		for (int i = 0; i < points.size(); i++) {
			int endIndex = (i == 0) ? points.size() - 1 : i - 1;
			list.add(new LineLatLng(points.get(i), points.get(endIndex)));
		}
		return list;
	}

	public void movePoint(Coord2D coord, int number) {
		points.get(number).set(coord);
	}

	public Area getArea() {
		return GeoTools.getArea(this);
	}

	/*
	 * @Override public List<LatLng> getPathPoints() { List<LatLng> path =
	 * getLatLngList(); if (getLatLngList().size() > 2) { path.add(path.get(0));
	 * } return path; }
	 */

	public void checkIfValid() throws Exception {
		if (points.size() < 3) {
			throw new InvalidPolygon(points.size());
		} else {
			return;
		}
	}

	public class InvalidPolygon extends Exception {
		private static final long serialVersionUID = 1L;
		public int size;

		public InvalidPolygon(int size) {
			this.size = size;
		}
	}

}
