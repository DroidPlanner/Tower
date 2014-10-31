package org.droidplanner.core.helpers.geoTools;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord2D;

public class PointTools {

	public static Coord2D findFarthestPoint(ArrayList<Coord2D> crosses, Coord2D middle) {
		double farthestDistance = Double.NEGATIVE_INFINITY;
		Coord2D farthestPoint = null;
		for (Coord2D cross : crosses) {
			double distance = GeoTools.getAproximatedDistance(cross, middle);
			if (distance > farthestDistance) {
				farthestPoint = cross;
				farthestDistance = distance;
			}
		}
		return farthestPoint;
	}

	/**
	 * Finds the closest point in a list to another point
	 * 
	 * @param point
	 *            point that will be used as reference
	 * @param list
	 *            List of points to be searched
	 * @return The closest point
	 */
	@SuppressWarnings("unused")
	private static Coord2D findClosestPoint(Coord2D point, List<Coord2D> list) {
		Coord2D answer = null;
		double currentbest = Double.MAX_VALUE;

		for (Coord2D pnt : list) {
			double dist1 = GeoTools.getAproximatedDistance(point, pnt);

			if (dist1 < currentbest) {
				answer = pnt;
				currentbest = dist1;
			}
		}
		return answer;
	}

	/**
	 * Finds the pair of adjacent points that minimize the distance to a
	 * reference point
	 * 
	 * @param point
	 *            point that will be used as reference
	 * @param waypoints2
	 *            List of points to be searched
	 * @return Position of the second point in the pair that minimizes the
	 *         distance
	 */
	static int findClosestPair(Coord2D point, List<Coord2D> waypoints2) {
		int answer = 0;
		double currentbest = Double.MAX_VALUE;
		double dist;
		Coord2D p1, p2;

		for (int i = 0; i < waypoints2.size(); i++) {
			if (i == waypoints2.size() - 1) {
				p1 = waypoints2.get(i);
				p2 = waypoints2.get(0);
			} else {
				p1 = waypoints2.get(i);
				p2 = waypoints2.get(i + 1);
			}

			dist = PointTools.pointToLineDistance(p1, p2, point);
			if (dist < currentbest) {
				answer = i + 1;
				currentbest = dist;
			}
		}
		return answer;
	}

	/**
	 * Provides the distance from a point P to the line segment that passes
	 * through A-B. If the point is not on the side of the line, returns the
	 * distance to the closest point
	 * 
	 * @param L1
	 *            First point of the line
	 * @param L2
	 *            Second point of the line
	 * @param P
	 *            Point to measure the distance
	 */
	public static double pointToLineDistance(Coord2D L1, Coord2D L2, Coord2D P) {
		double A = P.getX() - L1.getX();
		double B = P.getY() - L1.getY();
		double C = L2.getX() - L1.getX();
		double D = L2.getY() - L1.getY();

		double dot = A * C + B * D;
		double len_sq = C * C + D * D;
		double param = dot / len_sq;

		double xx, yy;

		if (param < 0) // point behind the segment
		{
			xx = L1.getX();
			yy = L1.getY();
		} else if (param > 1) // point after the segment
		{
			xx = L2.getX();
			yy = L2.getY();
		} else { // point on the side of the segment
			xx = L1.getX() + param * C;
			yy = L1.getY() + param * D;
		}

		return Math.hypot(xx - P.getX(), yy - P.getY());
	}

}
