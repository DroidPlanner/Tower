package com.droidplanner.helpers.geoTools.Dubins;

import java.util.List;

import android.util.Log;

import com.droidplanner.helpers.geoTools.GeoTools;
import com.droidplanner.helpers.geoTools.LineLatLng;
import com.google.android.gms.maps.model.LatLng;

public class PathLRL extends Path {

	private double thirdCircleAngle;
	private LatLng thirdCircle;
	private LineLatLng centerLine;
	private double secondTanAngle;
	private double firstTanAngle;
	private double endAngle;
	private double startAngle;

	public PathLRL(LineLatLng start, LineLatLng end, double radius) {
		super(start, end, radius);
		centerLine = new LineLatLng(circleStart, circleEnd);
		thirdCircleAngle = Math.toDegrees(Math.acos(GeoTools.getDistance(circleStart, circleEnd)/(4*radius)));
		thirdCircle = GeoTools.newCoordFromAngleAndDistance(circleStart,centerLine.getAngle()-thirdCircleAngle,radius*2);
		startAngle = startVector.getAngle()-getStartCircleAngle();
		endAngle = endVector.getAngle()-getEndCircleAngle();		
		firstTanAngle = centerLine.getAngle()-thirdCircleAngle;
		secondTanAngle = centerLine.getAngle()+thirdCircleAngle;
	}

	@Override
	protected double getPathLength() {
		double distance = 0; 
		distance += radius * Math.toRadians(DubinsMath.angularDistanceCCW(startAngle,firstTanAngle));
		distance += radius * Math.toRadians(180-2*thirdCircleAngle);
		distance += radius * Math.toRadians(DubinsMath.angularDistanceCCW(secondTanAngle-180,endAngle));
		return distance;
	}

	@Override
	protected List<LatLng> generatePoints() {
		Log.d("DUBIN", "Generating LRL path");
		
		List<LatLng> result = DubinsMath.generateArcCCW(circleStart,startAngle,firstTanAngle,radius);
		result.addAll(DubinsMath.generateArcCW(thirdCircle,firstTanAngle-180,secondTanAngle,radius));
		result.addAll(DubinsMath.generateArcCCW(circleEnd,secondTanAngle-180,endAngle,radius));
		return result;
	}

	@Override
	protected int getEndCircleAngle() {
		return LEFT_CIRCLE_ANGLE;
	}

	@Override
	protected int getStartCircleAngle() {
		return LEFT_CIRCLE_ANGLE;
	}

}
