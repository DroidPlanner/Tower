package com.droidplanner.fragments.helpers;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Point;

/**
 * Based on the Ramer–Douglas–Peucker algorithm algorithm
 * http://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm
 */
public class Simplify {
	public static List<Point> simplify(List<Point> list, double tolerance) {
		int index = 0;
		double dmax = 0;
		int lastIndex = list.size() - 1;

		// Find the point with the maximum distance
		for (int i = 1; i < list.size() - 1; i++) {
			double d = pointToLineDistance(list.get(0),
					list.get(lastIndex), list.get(i));
			if (d > dmax) {
				index = i;
				dmax = d;
			}
		}

		// If max distance is greater than epsilon, recursively simplify
		List<Point> ResultList = new ArrayList<Point>();
		if (dmax > tolerance) {
			// Recursive call
			List<Point> recResults1 = simplify(list.subList(0, index),
					tolerance);
			List<Point> recResults2 = simplify(
					list.subList(index, lastIndex), tolerance);

			// Build the result list
			recResults1.remove(recResults1.size()-1);
			ResultList.addAll(recResults1);
			ResultList.addAll(recResults2);
		} else {
			ResultList.add(list.get(0));
			ResultList.add(list.get(lastIndex));
		}
		// Return the result
		return ResultList;
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
	public static double pointToLineDistance(Point L1, Point L2, Point P) {
		double A = P.x - L1.y;
		double B = P.y - L1.y;
		double C = L2.x - L1.x;
		double D = L2.y - L1.y;

		double dot = A * C + B * D;
		double len_sq = C * C + D * D;
		double param = dot / len_sq;

		double xx, yy;

		if (param < 0) // point behind the segment
		{
			xx = L1.x;
			yy = L1.y;
		} else if (param > 1) // point after the segment
		{
			xx = L2.x;
			yy = L2.y;
		} else { // point on the side of the segment
			xx = L1.x + param * C;
			yy = L1.y + param * D;
		}

		return Math.hypot(xx - P.x, yy - P.y);
	}
}