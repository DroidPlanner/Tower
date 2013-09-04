package com.droidplanner.helpers.geoTools.Dubins;

import java.util.List;

import com.droidplanner.helpers.geoTools.LineLatLng;
import com.google.android.gms.maps.model.LatLng;

enum Path {
	RSR, RSL, LSR, LSL;

	private LineLatLng start;
	private LineLatLng end;
	private double radius;

	private void set(LineLatLng start, LineLatLng end, double radius) {
		this.start = start;
		this.end = end;
		this.radius = radius;
	}

	public static Path findShortestPath(LineLatLng start, LineLatLng end,
			double radius) {
		Path largestPath = null;
		double largest = Double.NEGATIVE_INFINITY;
		for (Path path : Path.values()) {
			path.set(start, end, radius);
			double length = path.getDistance();
			if (length > largest) {
				largest = length;
				largestPath = path;
			}
		}
		return largestPath;
	}

	public double getDistance() {
		switch (this) {
		default:
		case RSR:
			return 0;
		case LSL:
			return 0;
		case LSR:
			return 0;
		case RSL:
			return 0;
		}
	}

	public List<LatLng> generatePoints() {
		switch (this) {
		default:
		case RSR:
			return null;
		case LSL:
			return null;
		case LSR:
			return null;
		case RSL:
			return null;
		}
	}
}