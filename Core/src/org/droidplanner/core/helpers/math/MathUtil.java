package org.droidplanner.core.helpers.math;

import org.droidplanner.core.helpers.units.Length;

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

	public static double angleDiff(double a, double b) {
		double dif = Math.IEEEremainder(b - a + 180, 360);
		if (dif < 0)
			dif += 360;
		return dif - 180;
	}

	public static double constrainAngle(double x) {
		x = Math.IEEEremainder(x, 360);
		if (x < 0)
			x += 360;
		return x;
	}

	public static double bisectAngle(double a, double b, double alpha) {
		return constrainAngle(a + angleDiff(a, b) * alpha);
	}

	public static Length hypot(Length altDelta, Length distDelta) {
		return new Length(Math.hypot(altDelta.valueInMeters(), distDelta.valueInMeters()));
	}
}
