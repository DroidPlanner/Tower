package org.droidplanner.polygon;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.helpers.PolygonPoint;
import org.droidplanner.helpers.geoTools.GeoTools;
import org.droidplanner.helpers.geoTools.LineLatLng;
import org.droidplanner.helpers.units.Area;

import com.google.android.gms.maps.model.LatLng;

public class Polygon{

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

	public List<PolygonPoint> getPolygonPoints() {
		return points;
	}

	public void movePoint(LatLng coord, int number) {
		points.get(number).coord = coord;

	}

	public Area getArea() {
		return GeoTools.getArea(this);
	}

	/*
	@Override
	public List<LatLng> getPathPoints() {
		List<LatLng> path = getLatLngList();
		if (getLatLngList().size() > 2) {
			path.add(path.get(0));
		}
		return path;
	}*/

	public void checkIfValid() throws Exception {
		if (points.size()<3) {
			throw new InvalidPolygon(points.size());			
		}else{
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
