package com.droidplanner.helpers.geoTools.Dubins;

import java.util.ArrayList;
import java.util.List;

import com.droidplanner.helpers.geoTools.LineLatLng;
import com.google.android.gms.maps.model.LatLng;

abstract class Path {

	protected static final int RIGHT_CIRCLE_ANGLE = -90;
	protected static final int LEFT_CIRCLE_ANGLE = 90;

	protected abstract double getPathLength();

	protected abstract List<LatLng> generatePoints();

	protected abstract int getEndCircleAngle();

	protected abstract int getStartCircleAngle();

	protected LineLatLng startVector;
	protected LineLatLng endVector;
	protected double radius;
	protected LatLng circleStart;
	protected LatLng circleEnd;

	public Path(LineLatLng start, LineLatLng end, double radius) {
		this.startVector = start;
		this.endVector = end;
		this.radius = radius;
		circleStart = generateCircle(startVector, getStartCircleAngle());
		circleEnd = generateCircle(endVector, getEndCircleAngle());
	}

	public static Path findShortestPath(LineLatLng start, LineLatLng end,
			double radius) {
		List<Path> paths = new ArrayList<Path>();
		paths.add(new PathLSL(start, end, radius));
		paths.add(new PathRSL(start, end, radius));
		paths.add(new PathLSR(start, end, radius));
		paths.add(new PathRSR(start, end, radius));
		// TODO implement RLR and LRL paths

		Path shortestPath = null;
		double shortest = Double.POSITIVE_INFINITY;
		for (Path path : paths) {
			double length = path.getPathLength();
			if (length < shortest) {
				shortest = length;
				shortestPath = path;
			}
		}
		return shortestPath;
	}

	protected LatLng generateCircle(LineLatLng vector, int angle) {
		double dx = radius
				* Math.cos(Math.toRadians(vector.getHeading() + angle));
		double dy = radius
				* Math.sin(Math.toRadians(vector.getHeading() + angle));
		return new LatLng(vector.getEnd().longitude + dy,
				vector.getEnd().longitude + dx);
	}

}