package com.droidplanner.helpers.geoTools.spline;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;

public class SplinePath {

	private List<LatLng> points;
	private List<LatLng> result;
	private int decimation;

	public List<LatLng> process(List<LatLng> points, int n) {
		this.points = points;
		decimation = n;
		try {
			tryToProcessPath();
		} catch (Exception e) {
			result = points;
		}
		return result;
	}

	private void tryToProcessPath() throws Exception {
		if (points.size() < 4) {
			throw new Exception("Not enough points");
		}

		result = new ArrayList<LatLng>();
		result.add(points.get(0));
		processPath();
		result.add(points.get(points.size() - 1));
	}

	private void processPath() {
		for (int i = 3; i < points.size(); i++) {
			processPathSegment(points.get(i - 3), points.get(i - 2),
					points.get(i - 1), points.get(i));
		}
	}

	private void processPathSegment(LatLng l1, LatLng l2, LatLng l3, LatLng l4) {
		Spline spline = new Spline(new Vector2D(l1), new Vector2D(l2),
				new Vector2D(l3), new Vector2D(l4));
		result.addAll(spline.generateCoordinates(decimation));
	}

}
