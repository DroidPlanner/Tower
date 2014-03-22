package org.droidplanner.core.helpers.math;

public class MathUtil {

	private static double Constrain(double value, double min, double max) {
		value = Math.max(value, min);
		value = Math.min(value, max);
		return value;
	}

	public static double Normalize(double value, double min, double max) {
		value = Constrain(value, min, max);
		return (value - min) / (max - min);

	}

}
