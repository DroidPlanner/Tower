package com.droidplanner.helpers.geoTools.Dubins;

import java.util.List;

import android.util.Log;

import com.droidplanner.helpers.geoTools.GeoTools;
import com.droidplanner.helpers.geoTools.LineLatLng;
import com.google.android.gms.maps.model.LatLng;

public class PathLSL extends Path {

	public PathLSL(LineLatLng start, LineLatLng end, double radius) {
		super(start, end, radius);
	}

	@Override
	protected double getPathLength() {
		double interCircleAngle = new LineLatLng(circleStart, circleEnd)
				.getAngle();
		double distance = GeoTools.getAproximatedDistance(circleStart,
				circleEnd);
		distance += radius * DubinsMath.angularDistanceCCW(startVector.getAngle(),interCircleAngle);
		distance += radius * DubinsMath.angularDistanceCCW(interCircleAngle,endVector.getAngle());
		return distance;
	}

	@Override
	protected List<LatLng> generatePoints() {
		// TODO Auto-generated method stub
		Log.d("DUBIN", "Generating LSL path");
		return super.generatePoints();
	}

	protected int getEndCircleAngle() {
		return LEFT_CIRCLE_ANGLE;
	}

	@Override
	protected int getStartCircleAngle() {
		return LEFT_CIRCLE_ANGLE;
	}
}
