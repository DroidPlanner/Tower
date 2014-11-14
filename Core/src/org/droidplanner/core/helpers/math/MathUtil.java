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
	
	/** 
	 * Create a rotation matrix given some euler angles
	 * this is based on http://gentlenav.googlecode.com/files/EulerAngles.pdf 
	 */
	public static double [][] dcmFromEuler(double roll, double pitch, double yaw)
	{
		double dcm[][] = new double[3][3];
		
	    double cp = Math.cos(pitch);
	    double sp = Math.sin(pitch);
	    double sr = Math.sin(roll);
	    double cr = Math.cos(roll);
	    double sy = Math.sin(yaw);
	    double cy = Math.cos(yaw);

	    dcm[0][0] = cp * cy;
	    dcm[1][0] = (sr * sp * cy) - (cr * sy);
	    dcm[2][0] = (cr * sp * cy) + (sr * sy);
	    dcm[0][1] = cp * sy;
	    dcm[1][1] = (sr * sp * sy) + (cr * cy);
	    dcm[2][1] = (cr * sp * sy) - (sr * cy);
	    dcm[0][2] = -sp;
	    dcm[1][2] = sr * cp;
	    dcm[2][2] = cr * cp;
	    
	    return dcm;
	}
}
