package org.droidplanner.core.helpers.geoTools.spline;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord2D;

public class Spline {

	private static final double SPLINE_TENSION = 1.6;

	private Coord2D p0;
	private Coord2D p0_prime;
	private Coord2D a;
	private Coord2D b;

	public Spline(Coord2D pMinus1, Coord2D p0, Coord2D p1, Coord2D p2) {
		this.p0 = p0;

		// derivative at a point is based on difference of previous and next
		// points
		p0_prime = p1.subtract(pMinus1).dot(1 / SPLINE_TENSION);
		Coord2D p1_prime = p2.subtract(this.p0).dot(1 / SPLINE_TENSION);

		// compute a and b coords used in spline formula
		a = Coord2D.sum(this.p0.dot(2), p1.dot(-2), p0_prime, p1_prime);
		b = Coord2D.sum(this.p0.dot(-3), p1.dot(3), p0_prime.dot(-2), p1_prime.negate());
	}

	public List<Coord2D> generateCoordinates(int decimation) {
		ArrayList<Coord2D> result = new ArrayList<Coord2D>();
		float step = 1f / decimation;
		for (float i = 0; i < 1; i += step) {
			result.add(evaluate(i));
		}

		return result;
	}

	private Coord2D evaluate(double t) {
		double tSquared = t * t;
		double tCubed = tSquared * t;

		return Coord2D.sum(a.dot(tCubed), b.dot(tSquared), p0_prime.dot(t), p0);
	}

}
