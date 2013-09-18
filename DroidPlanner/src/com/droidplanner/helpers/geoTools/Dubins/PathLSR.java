package com.droidplanner.helpers.geoTools.Dubins;

import java.util.List;

import android.util.Log;

import com.droidplanner.helpers.geoTools.GeoTools;
import com.droidplanner.helpers.geoTools.LineLatLng;
import com.google.android.gms.maps.model.LatLng;

public class PathLSR extends Path {

	public PathLSR(LineLatLng start, LineLatLng end, double radius) {
		super(start, end, radius);
	}

	@Override
	protected double getPathLength() {
		double interCircleAngle = new LineLatLng(circleStart, circleEnd).getAngle();
		double distanceBetweenCenters = GeoTools.getAproximatedDistance(circleStart,circleEnd);
		double distance = Math.sqrt(distanceBetweenCenters*distanceBetweenCenters-4*radius*radius);
		distance += radius * Math.toRadians(DubinsMath.angularDistanceCCW(startVector.getAngle(),interCircleAngle));
		distance += radius * Math.toRadians(DubinsMath.angularDistanceCW(interCircleAngle,endVector.getAngle()));
		return distance;
	}

	@Override
	protected List<LatLng> generatePoints() {
		Log.d("DUBIN", "Generating LSL path");
		double interCircleAngle = new LineLatLng(circleStart, circleEnd).getAngle();
		double startAngle = startVector.getAngle()-getStartCircleAngle();
		double endAngle = endVector.getAngle()-getEndCircleAngle();
		List<LatLng> result = DubinsMath.generateArcCCW(circleStart,startAngle, interCircleAngle-getStartCircleAngle(),radius);
		result.addAll(DubinsMath.generateArcCW(circleEnd,interCircleAngle-getEndCircleAngle(), endAngle,radius));		
		return result;
	}

	protected int getEndCircleAngle() {
		return LEFT_CIRCLE_ANGLE;
	}

	@Override
	protected int getStartCircleAngle() {
		return RIGHT_CIRCLE_ANGLE;
	}
}
