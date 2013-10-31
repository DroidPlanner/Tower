package com.droidplanner.helpers.geoTools.Dubins;

import java.util.ArrayList;
import java.util.List;

import com.droidplanner.helpers.geoTools.LineLatLng;
import com.google.android.gms.maps.model.LatLng;

public class Dubins {

	private double radius;
	private List<LatLng> result;

	public Dubins(double radius) {
		this.radius = radius;
	}

	public List<LatLng> generate(List<LatLng> points) {
		try {
			tryToProcessPath(points);
		} catch (Exception e) {
			result = points;
		}
		return result;
	}

	private void tryToProcessPath(List<LatLng> points) throws Exception {
		if (points.size() < 3) {
			throw new Exception("Not enough points");
		}
		
		result = new ArrayList<LatLng>();
		result.add(points.get(0));
		processPath(points);
		result.add(points.get(points.size() - 1));
	}

	private void processPath(List<LatLng> points) {
		for (int i = 2; i < points.size(); i++) {
			LineLatLng start = new LineLatLng(points.get(i - 2),
					points.get(i - 1));
			LineLatLng end = new LineLatLng(points.get(i - 1), points.get(i));
			processPathSegment(start, end);
		}
	}

	private void processPathSegment(LineLatLng start, LineLatLng end) {
		Path shortestPath = Path.findShortestPath(start, end, radius);
		result.addAll(shortestPath.generatePoints());
	}
}
