package com.droidplanner.helpers.geoTools.Dubins;

import java.util.ArrayList;
import java.util.List;

import com.droidplanner.helpers.geoTools.GeoTools;
import com.google.android.gms.maps.model.LatLng;

public class DubinsMath {

	private static final int ARC_STEP = 10;

	public static double angularDistanceCW(double start, double end) {
		return angularDistanceCCW(end, start);
	}

	public static double angularDistanceCCW(double start, double end) {
		return (end - start + 360) % 360;
	}

	public static List<LatLng> generateArcCCW(LatLng center,
			double startAngle, double endAngle, double radius) {
		double steps = angularDistanceCCW(startAngle, endAngle)/ARC_STEP;
		ArrayList<LatLng> result = new ArrayList<LatLng>();
		for (int i = 0; i < steps; i++) {
			result.add(GeoTools.newCoordFromAngleAndDistance(center,(startAngle+i*ARC_STEP+360)%360, radius));
		}
		result.add(GeoTools.newCoordFromAngleAndDistance(center,endAngle, radius));
		return result;
	}

	public static List<LatLng> generateArcCW(LatLng center,
			double startAngle, double endAngle, double radius){
		double steps = angularDistanceCW(startAngle, endAngle)/ARC_STEP;
		ArrayList<LatLng> result = new ArrayList<LatLng>();
		for (int i = 0; i < steps; i++) {
			result.add(GeoTools.newCoordFromAngleAndDistance(center,(startAngle-i*ARC_STEP+360)%360, radius));
		}
		result.add(GeoTools.newCoordFromAngleAndDistance(center,endAngle, radius));
		return result;
	}

}
