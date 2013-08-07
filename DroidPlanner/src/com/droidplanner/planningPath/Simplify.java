package com.droidplanner.planningPath;

import java.util.ArrayList;
import java.util.List;

import com.droidplanner.polygon.GeoTools;
import com.google.android.gms.maps.model.LatLng;

/**
 * Based on the Ramer–Douglas–Peucker algorithm algorithm
 * http://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm
 */
public class Simplify {
	public static List<LatLng> simplify(List<LatLng> PointList, double tolerance) {
		int index = 0;
		double dmax = 0;
		int lastIndex = PointList.size() - 1;

		// Find the point with the maximum distance
		for (int i = 1; i < PointList.size() - 1; i++) {
			double d = GeoTools.pointToLineDistance(PointList.get(0),
					PointList.get(lastIndex), PointList.get(i));
			if (d > dmax) {
				index = i;
				dmax = d;
			}
		}

		// If max distance is greater than epsilon, recursively simplify
		List<LatLng> ResultList = new ArrayList<LatLng>();
		if (dmax > tolerance) {
			// Recursive call
			List<LatLng> recResults1 = simplify(PointList.subList(0, index),
					tolerance);
			List<LatLng> recResults2 = simplify(
					PointList.subList(index, lastIndex), tolerance);

			// Build the result list
			recResults1.remove(recResults1.size()-1);
			ResultList.addAll(recResults1);
			ResultList.addAll(recResults2);
		} else {
			ResultList.add(PointList.get(0));
			ResultList.add(PointList.get(lastIndex));
		}
		// Return the result
		return ResultList;
	}
}