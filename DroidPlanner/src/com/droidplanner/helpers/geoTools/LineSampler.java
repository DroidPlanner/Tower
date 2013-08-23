package com.droidplanner.helpers.geoTools;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;

public class LineSampler {

	private List<LatLng> points;
	private List<LatLng> sampledPoints = new ArrayList<LatLng>();

	public LineSampler(List<LatLng> points) {
		this.points = points;
	}

	public List<LatLng> sample(double sampleDistance) {
		for (int i = 1; i < points.size(); i++) {
			LatLng from = points.get(i - 1);
			LatLng to = points.get(i);
			sampledPoints.addAll(sampleLine(from, to, sampleDistance));
		}
		sampledPoints.add(getLast(points));
		return sampledPoints;
	}


	private List<LatLng> sampleLine(LatLng from, LatLng to,
			double samplingDistance) {
		List<LatLng> result = new ArrayList<LatLng>();
		double heading = GeoTools.getHeadingFromCoordinates(from, to);
		double totalLength = GeoTools.getDistance(from, to);
		double distance = 0;
		
		while (distance < totalLength) {
			GeoTools.newpos(from, heading, distance);
			distance += samplingDistance;
		}
		return result;
	}
	
	private LatLng getLast(List<LatLng> list) {
		return list.get(list.size()-1);
	}

}
