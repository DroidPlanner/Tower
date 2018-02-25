package org.droidplanner.services.android.impl.core.helpers.geoTools.spline;

import com.o3dr.services.android.lib.coordinate.LatLong;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains functions used to generate a spline path.
 */
public class SplinePath {

	/**
	 * Used as tag for logging.
	 */
	private static final String TAG = SplinePath.class.getSimpleName();

	private final static int SPLINE_DECIMATION = 20;

	/**
	 * Process the given map coordinates, and return a set of coordinates
	 * describing the spline path.
	 * 
	 * @param points
	 *            map coordinates decimation factor
	 * @return set of coordinates describing the spline path
	 */
	public static List<LatLong> process(List<LatLong> points) {
		final int pointsCount = points.size();
		if (pointsCount < 4) {
			System.err.println("Not enough points!");
			return points;
		}

		final List<LatLong> results = processPath(points);
		results.add(0, points.get(0));
		results.add(points.get(pointsCount - 1));
		return results;
	}

	private static List<LatLong> processPath(List<LatLong> points) {
		final List<LatLong> results = new ArrayList<LatLong>();
		for (int i = 3; i < points.size(); i++) {
			results.addAll(processPathSegment(points.get(i - 3), points.get(i - 2),
					points.get(i - 1), points.get(i)));
		}
		return results;
	}

	private static List<LatLong> processPathSegment(LatLong l1, LatLong l2, LatLong l3, LatLong l4) {
		Spline spline = new Spline(l1, l2, l3, l4);
		return spline.generateCoordinates(SPLINE_DECIMATION);
	}

}
