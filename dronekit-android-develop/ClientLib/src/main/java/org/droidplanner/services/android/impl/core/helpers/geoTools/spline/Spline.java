package org.droidplanner.services.android.impl.core.helpers.geoTools.spline;

import com.o3dr.services.android.lib.coordinate.LatLong;

import java.util.ArrayList;
import java.util.List;

public class Spline {

	private static final double SPLINE_TENSION = 1.6;

	private LatLong p0;
	private LatLong p0_prime;
	private LatLong a;
	private LatLong b;

	public Spline(LatLong pMinus1, LatLong p0, LatLong p1, LatLong p2) {
		this.p0 = p0;

		// derivative at a point is based on difference of previous and next
		// points
		p0_prime = p1.subtract(pMinus1).dot(1 / SPLINE_TENSION);
		LatLong p1_prime = p2.subtract(this.p0).dot(1 / SPLINE_TENSION);

		// compute a and b coords used in spline formula
		a = LatLong.sum(this.p0.dot(2), p1.dot(-2), p0_prime, p1_prime);
		b = LatLong.sum(this.p0.dot(-3), p1.dot(3), p0_prime.dot(-2), p1_prime.negate());
	}

	public List<LatLong> generateCoordinates(int decimation) {
		ArrayList<LatLong> result = new ArrayList<LatLong>();
		float step = 1f / decimation;
		for (float i = 0; i < 1; i += step) {
			result.add(evaluate(i));
		}

		return result;
	}

	private LatLong evaluate(double t) {
		double tSquared = t * t;
		double tCubed = tSquared * t;

		return LatLong.sum(a.dot(tCubed), b.dot(tSquared), p0_prime.dot(t), p0);
	}

}
