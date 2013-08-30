package com.droidplanner.helpers.geoTools;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;

public class PointTools {

	public static LatLng findFarthestPoint(ArrayList<LatLng> crosses,
			LatLng middle) {
		double farthestDistance = Double.NEGATIVE_INFINITY;
		LatLng farthestPoint = null;
		for (LatLng cross : crosses) {
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
	private static LatLng findClosestPoint(LatLng point, List<LatLng> list) {
		LatLng answer = null;
		double currentbest = Double.MAX_VALUE;

		for (LatLng pnt : list) {
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
	static int findClosestPair(LatLng point, List<LatLng> waypoints2) {
		int answer = 0;
		double currentbest = Double.MAX_VALUE;
		double dist;
		LatLng p1, p2;

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
	public static double pointToLineDistance(LatLng L1, LatLng L2, LatLng P) {
		double A = P.longitude - L1.longitude;
		double B = P.latitude - L1.latitude;
		double C = L2.longitude - L1.longitude;
		double D = L2.latitude - L1.latitude;

		double dot = A * C + B * D;
		double len_sq = C * C + D * D;
		double param = dot / len_sq;

		double xx, yy;

		if (param < 0) // point behind the segment
		{
			xx = L1.longitude;
			yy = L1.latitude;
		} else if (param > 1) // point after the segment
		{
			xx = L2.longitude;
			yy = L2.latitude;
		} else { // point on the side of the segment
			xx = L1.longitude + param * C;
			yy = L1.latitude + param * D;
		}

		return Math.hypot(xx - P.longitude, yy - P.latitude);
	}

}
