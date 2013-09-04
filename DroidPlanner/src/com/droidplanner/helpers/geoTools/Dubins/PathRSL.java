package com.droidplanner.helpers.geoTools.Dubins;

import java.util.List;

import com.droidplanner.helpers.geoTools.LineLatLng;
import com.google.android.gms.maps.model.LatLng;

public class PathRSL extends Path {

	public PathRSL(LineLatLng start, LineLatLng end, double radius) {
		super(start, end, radius);
	}

	@Override
	protected double getDistance() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected List<LatLng> generatePoints() {
		// TODO Auto-generated method stub
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
