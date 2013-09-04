package com.droidplanner.helpers.geoTools.Dubins;

import java.util.List;

import android.util.Log;

import com.droidplanner.helpers.geoTools.LineLatLng;
import com.google.android.gms.maps.model.LatLng;

public class PathRSL extends Path {

	public PathRSL(LineLatLng start, LineLatLng end, double radius) {
		super(start, end, radius);
	}

	@Override
	protected double getPathLength() {
		// TODO Auto-generated method stub
		return Double.MAX_VALUE;
	}

	@Override
	protected List<LatLng> generatePoints() {
		// TODO Auto-generated method stub
		Log.d("DUBIN", "Generating RSL path");
		return null;
	}

	protected int getEndCircleAngle() {
		return RIGHT_CIRCLE_ANGLE;
	}

	@Override
	protected int getStartCircleAngle() {
		return LEFT_CIRCLE_ANGLE;
	}

}
