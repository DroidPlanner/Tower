package org.droidplanner.services.android.impl.core.helpers.geoTools;

import com.o3dr.services.android.lib.coordinate.LatLong;

import java.util.ArrayList;
import java.util.List;

public class PointTools {

	public static LatLong findFarthestPoint(ArrayList<LatLong> crosses, LatLong middle) {
		double farthestDistance = Double.NEGATIVE_INFINITY;
		LatLong farthestPoint = null;
		for (LatLong cross : crosses) {
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
	private static LatLong findClosestPoint(LatLong point, List<LatLong> list) {
		LatLong answer = null;
		double currentbest = Double.MAX_VALUE;

		for (LatLong pnt : list) {
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
	static int findClosestPair(LatLong point, List<LatLong> waypoints2) {
		int answer = 0;
		double currentbest = Double.MAX_VALUE;
		double dist;
		LatLong p1, p2;

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
	public static double pointToLineDistance(LatLong L1, LatLong L2, LatLong P) {
		double A = P.getLatitude() - L1.getLatitude();
		double B = P.getLongitude() - L1.getLongitude();
		double C = L2.getLatitude() - L1.getLatitude();
		double D = L2.getLongitude() - L1.getLongitude();

		double dot = A * C + B * D;
		double len_sq = C * C + D * D;
		double param = dot / len_sq;

		double xx, yy;

		if (param < 0) // point behind the segment
		{
			xx = L1.getLatitude();
			yy = L1.getLongitude();
		} else if (param > 1) // point after the segment
		{
			xx = L2.getLatitude();
			yy = L2.getLongitude();
		} else { // point on the side of the segment
			xx = L1.getLatitude() + param * C;
			yy = L1.getLongitude() + param * D;
		}

		return Math.hypot(xx - P.getLatitude(), yy - P.getLongitude());
	}

}
