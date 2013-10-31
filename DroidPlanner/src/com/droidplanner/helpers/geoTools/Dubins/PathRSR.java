package com.droidplanner.helpers.geoTools.Dubins;

import java.util.List;

import android.util.Log;

import com.droidplanner.helpers.geoTools.GeoTools;
import com.droidplanner.helpers.geoTools.LineLatLng;
import com.google.android.gms.maps.model.LatLng;

public class PathRSR extends Path {

	public PathRSR(LineLatLng start, LineLatLng end, double radius) {
		super(start, end, radius);
	}

	@Override
	protected double getPathLength() {
		double interCircleAngle = new LineLatLng(circleStart, circleEnd)
				.getAngle();
		double distance = GeoTools.getDistance(circleStart,
				circleEnd);
		distance += radius * Math.toRadians(DubinsMath.angularDistanceCW(startVector.getAngle(),interCircleAngle));
		distance += radius * Math.toRadians(DubinsMath.angularDistanceCW(interCircleAngle,endVector.getAngle()));
		return distance;
	}

	@Override
	protected List<LatLng> generatePoints() {
		// TODO Auto-generated method stub
		Log.d("DUBIN", "Generating RSR path");
		double interCircleAngle = new LineLatLng(circleStart, circleEnd).getAngle();
		double startAngle = startVector.getAngle()-getStartCircleAngle();
		double endAngle = endVector.getAngle()-getEndCircleAngle();
		List<LatLng> result = DubinsMath.generateArcCW(circleStart,startAngle, interCircleAngle-getStartCircleAngle(),radius);
		result.addAll(DubinsMath.generateArcCW(circleEnd,interCircleAngle-getEndCircleAngle(), endAngle,radius));
		return result;
	}

	@Override
	protected int getEndCircleAngle() {
		return RIGHT_CIRCLE_ANGLE;
	}

	@Override
	protected int getStartCircleAngle() {
		return RIGHT_CIRCLE_ANGLE;
	}

}
