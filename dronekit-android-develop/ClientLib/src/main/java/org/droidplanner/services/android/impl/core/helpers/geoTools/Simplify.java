package org.droidplanner.services.android.impl.core.helpers.geoTools;

import com.o3dr.services.android.lib.coordinate.LatLong;

import java.util.ArrayList;
import java.util.List;

/**
 * Based on the Ramer–Douglas–Peucker algorithm algorithm
 * http://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm
 */
public class Simplify {
	public static List<LatLong> simplify(List<LatLong> list, double tolerance) {
		int index = 0;
		double dmax = 0;
		int lastIndex = list.size() - 1;

		// Find the point with the maximum distance
		for (int i = 1; i < lastIndex; i++) {
			double d = PointTools
					.pointToLineDistance(list.get(0), list.get(lastIndex), list.get(i));
			if (d > dmax) {
				index = i;
				dmax = d;
			}
		}

		// If max distance is greater than epsilon, recursively simplify
		List<LatLong> ResultList = new ArrayList<LatLong>();
		if (dmax > tolerance) {
			// Recursive call
			List<LatLong> recResults1 = simplify(list.subList(0, index + 1), tolerance);
			List<LatLong> recResults2 = simplify(list.subList(index, lastIndex + 1), tolerance);

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
}