package org.droidplanner.core.helpers.geoTools;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord2D;

public class LineSampler {

	private List<Coord2D> points;
	private List<Coord2D> sampledPoints = new ArrayList<Coord2D>();

	public LineSampler(List<Coord2D> points) {
		this.points = points;
	}

	public LineSampler(Coord2D p1, Coord2D p2) {
		points = new ArrayList<Coord2D>();
		points.add(p1);
		points.add(p2);
	}

	public List<Coord2D> sample(double sampleDistance) {
		for (int i = 1; i < points.size(); i++) {
			Coord2D from = points.get(i - 1);
			Coord2D to = points.get(i);
			sampledPoints.addAll(sampleLine(from, to, sampleDistance));
		}
		sampledPoints.add(getLast(points));
		return sampledPoints;
	}

	private List<Coord2D> sampleLine(Coord2D from, Coord2D to,
			double samplingDistance) {
		List<Coord2D> result = new ArrayList<Coord2D>();
		double heading = GeoTools.getHeadingFromCoordinates(from, to);
		double totalLength = GeoTools.getDistance(from, to).valueInMeters();
		double distance = 0;

		while (distance < totalLength) {
			result.add(GeoTools.newCoordFromBearingAndDistance(from, heading,
					distance));
			distance += samplingDistance;
		}
		return result;
	}

	private Coord2D getLast(List<Coord2D> list) {
		return list.get(list.size() - 1);
	}

}
