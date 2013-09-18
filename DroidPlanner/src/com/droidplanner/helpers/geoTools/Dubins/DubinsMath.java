package com.droidplanner.helpers.geoTools.Dubins;

public class DubinsMath {
	public static final double TWO_PI = Math.PI * 2;

	public static double angularDistanceCW(double start, double end) {
		return angularDistanceCCW(end, start);
	}

	public static double angularDistanceCCW(double start, double end) {
		return (end - start + TWO_PI) % TWO_PI;
	}

}
