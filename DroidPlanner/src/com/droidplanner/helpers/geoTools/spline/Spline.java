package com.droidplanner.helpers.geoTools.spline;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;

public class Spline {

	private static final double SPLINE_TENSION = 1.6;

	private Vector2D p0;
	private Vector2D p0_prime;
	private Vector2D a;
	private Vector2D b;

	public Spline(Vector2D pMinus1, Vector2D p0, Vector2D p1, Vector2D p2) {
		this.p0 = p0;
		Vector2D p1_prime;

		// derivative at a point is based on difference of previous
		// and next points
		p0_prime = p1.subtract(pMinus1).dot(1 / SPLINE_TENSION);
		p1_prime = p2.subtract(this.p0).dot(1 / SPLINE_TENSION);

		// compute a and b vectors used in spline formula
		a = Vector2D.sum(this.p0.dot(2), p1.dot(-2), p0_prime, p1_prime);
		b = Vector2D.sum(this.p0.dot(-3), p1.dot(3), p0_prime.dot(-2),
				p1_prime.dot(-1));
	}

	public List<LatLng> generateCoordinates(int decimation) {
		ArrayList<LatLng> result = new ArrayList<LatLng>();
		float step = 1f / decimation;
		for (float i = 0; i < 1; i += step) {
			result.add(evaluate(i).getCoordinate());
		}

		return result;
	}

	private Vector2D evaluate(double t) {
		double tSquared = t * t;
		double tCubed = tSquared * t;

		return Vector2D
				.sum(a.dot(tCubed), b.dot(tSquared), p0_prime.dot(t), p0);
	}

}
