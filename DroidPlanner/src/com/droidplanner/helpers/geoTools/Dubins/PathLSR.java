package com.droidplanner.helpers.geoTools.Dubins;

import java.util.List;

import com.droidplanner.helpers.geoTools.LineLatLng;
import com.google.android.gms.maps.model.LatLng;

public class PathLSR extends Path {

	public PathLSR(LineLatLng start, LineLatLng end, double radius) {
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
		return LEFT_CIRCLE_ANGLE;
	}

	@Override
	protected int getStartCircleAngle() {
		return RIGHT_CIRCLE_ANGLE;
	}
}
