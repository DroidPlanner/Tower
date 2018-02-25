package org.droidplanner.services.android.impl.core.helpers.geoTools;

import com.o3dr.services.android.lib.coordinate.LatLong;

import java.util.ArrayList;
import java.util.List;

public class LineSampler {

	private List<LatLong> points;
	private List<LatLong> sampledPoints = new ArrayList<>();

	public LineSampler(List<LatLong> points) {
		this.points = points;
	}

	public LineSampler(LatLong p1, LatLong p2) {
		points = new ArrayList<>();
		points.add(p1);
		points.add(p2);
	}

	public List<LatLong> sample(double sampleDistance) {
		for (int i = 1; i < points.size(); i++) {
			LatLong from = points.get(i - 1);
			if (from == null) {
				continue;
			}

			LatLong to = points.get(i);
			sampledPoints.addAll(sampleLine(from, to, sampleDistance));
		}

		final LatLong lastPoint = getLast(points);
		if (lastPoint != null) {
			sampledPoints.add(lastPoint);
		}
		return sampledPoints;
	}

	private List<LatLong> sampleLine(LatLong from, LatLong to, double samplingDistance) {
		List<LatLong> result = new ArrayList<LatLong>();
		double heading = GeoTools.getHeadingFromCoordinates(from, to);
		double totalLength = GeoTools.getDistance(from, to);
		double distance = 0;

		while (distance < totalLength) {
			result.add(GeoTools.newCoordFromBearingAndDistance(from, heading, distance));
			distance += samplingDistance;
		}
		return result;
	}

	private LatLong getLast(List<LatLong> list) {
		return list.get(list.size() - 1);
	}

}
