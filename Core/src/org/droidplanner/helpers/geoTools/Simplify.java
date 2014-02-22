package org.droidplanner.helpers.geoTools;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Based on the Ramer–Douglas–Peucker algorithm algorithm
 * http://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm
 */
public class Simplify {
	public static List<Point> simplify(List<Point> list, double tolerance) {
		int index = 0;
		double dmax = 0;
		double squareTolerance = tolerance*tolerance;
		int lastIndex = list.size() - 1;

		// Find the point with the maximum distance
		for (int i = 1; i < list.size() - 1; i++) {
			double d = pointToLineDistance(list.get(0), list.get(lastIndex),
					list.get(i));
			if (d > dmax) {
				index = i;
				dmax = d;
			}
		}

		// If max distance is greater than epsilon, recursively simplify
		List<Point> ResultList = new ArrayList<Point>();
		if (dmax > squareTolerance) {
			// Recursive call
			List<Point> recResults1 = simplify(list.subList(0, index + 1),
					tolerance);
			List<Point> recResults2 = simplify(
					list.subList(index, lastIndex + 1), tolerance);

			// Build the result list
			recResults1.remove(recResults1.size() - 1);
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
	 * Perpendicular Distance of point to line
	 * 
	 * @param L1
	 *            First point of the line
	 * @param L2
	 *            Second point of the line
	 * @param P
	 *            Point to measure the distance
	 * @return The square distance
	 */
	public static double pointToLineDistance(Point L1, Point L2, Point P) {
		double x0, y0, x1, y1, x2, y2, dx, dy, t;

		x1 = L1.x;
		y1 = L1.y;
		x2 = L2.x;
		y2 = L2.y;
		x0 = P.x;
		y0 = P.y;

		dx = x2 - x1;
		dy = y2 - y1;

		if (dx != 0.0d || dy != 0.0d) {
			t = ((x0 - x1) * dx + (y0 - y1) * dy) / (dx * dx + dy * dy);

			if (t > 1.0d) {
				x1 = x2;
				y1 = y2;
			} else if (t > 0.0d) {
				x1 += dx * t;
				y1 += dy * t;
			}
		}

		dx = x0 - x1;
		dy = y0 - y1;

		return dx * dx + dy * dy;
	}
}