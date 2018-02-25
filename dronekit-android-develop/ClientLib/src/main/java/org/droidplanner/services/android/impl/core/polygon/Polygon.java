package org.droidplanner.services.android.impl.core.polygon;

import org.droidplanner.services.android.impl.core.helpers.geoTools.GeoTools;
import org.droidplanner.services.android.impl.core.helpers.geoTools.LineLatLong;
import org.droidplanner.services.android.impl.core.helpers.units.Area;
import com.o3dr.services.android.lib.coordinate.LatLong;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Polygon {

	private List<LatLong> points = new ArrayList<LatLong>();

	public void addPoints(List<LatLong> pointList) {
		for (LatLong point : pointList) {
			addPoint(point);
		}
	}

	public void addPoint(LatLong coord) {
		points.add(coord);
	}

	public void clearPolygon() {
		points.clear();
	}

	public List<LatLong> getPoints() {
		return points;
	}

	public List<LineLatLong> getLines() {
		List<LineLatLong> list = new ArrayList<LineLatLong>();
		for (int i = 0; i < points.size(); i++) {
			int endIndex = (i == 0) ? points.size() - 1 : i - 1;
			list.add(new LineLatLong(points.get(i), points.get(endIndex)));
		}
		return list;
	}

	public void movePoint(LatLong coord, int number) {
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
		}
	}

	public class InvalidPolygon extends Exception {
		private static final long serialVersionUID = 1L;
		public int size;

		public InvalidPolygon(int size) {
			this.size = size;
		}
	}

	public void reversePoints() {
		Collections.reverse(points);
	}

}
