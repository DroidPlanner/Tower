package org.droidplanner.helpers.geoTools;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.polygon.PolyBounds;

import com.google.android.gms.maps.model.LatLng;

public class LineTools {

	public static LineLatLng findExternalPoints(ArrayList<LatLng> crosses) {
		LatLng meanCoord = new PolyBounds(crosses).getMiddle();
		LatLng start = PointTools.findFarthestPoint(crosses, meanCoord);
		LatLng end = PointTools.findFarthestPoint(crosses, start);
		return new LineLatLng(start, end);
	}

	/**
	 * Finds the intersection of two lines http://stackoverflow.com/questions/
	 * 1119451/how-to-tell-if-a-line-intersects -a-polygon-in-c
	 * 
	 * @throws Exception
	 */
	public static LatLng FindLineIntersection(LineLatLng first,
			LineLatLng second) throws Exception {
		double denom = ((first.p2.longitude - first.p1.longitude) * (second.p2.latitude - second.p1.latitude))
				- ((first.p2.latitude - first.p1.latitude) * (second.p2.longitude - second.p1.longitude));
		if (denom == 0)
			throw new Exception("Parralel Lines");
		double numer = ((first.p1.latitude - second.p1.latitude) * (second.p2.longitude - second.p1.longitude))
				- ((first.p1.longitude - second.p1.longitude) * (second.p2.latitude - second.p1.latitude));
		double r = numer / denom;
		double numer2 = ((first.p1.latitude - second.p1.latitude) * (first.p2.longitude - first.p1.longitude))
				- ((first.p1.longitude - second.p1.longitude) * (first.p2.latitude - first.p1.latitude));
		double s = numer2 / denom;
		if ((r < 0 || r > 1) || (s < 0 || s > 1))
			throw new Exception("No Intersection");
		// Find intersection point
		double longitude = first.p1.longitude
				+ (r * (first.p2.longitude - first.p1.longitude));
		double latitude = first.p1.latitude
				+ (r * (first.p2.latitude - first.p1.latitude));
		return (new LatLng(latitude, longitude));
	}

	/**
	 * Finds the line that has the start or tip closest to a point.
	 * 
	 * @param point
	 *            Point to the distance will be minimized
	 * @param list
	 *            A list of lines to search
	 * @return The closest Line
	 */
	public static LineLatLng findClosestLineToPoint(LatLng point,
			List<LineLatLng> list) {
		LineLatLng answer = list.get(0);
		double shortest = Double.MAX_VALUE;

		for (LineLatLng line : list) {
			double ans1 = GeoTools.getAproximatedDistance(point, line.p1);
			double ans2 = GeoTools.getAproximatedDistance(point, line.p2);
			LatLng shorterpnt = ans1 < ans2 ? line.p1 : line.p2;

			if (shortest > GeoTools.getAproximatedDistance(point, shorterpnt)) {
				answer = line;
				shortest = GeoTools.getAproximatedDistance(point, shorterpnt);
			}
		}
		return answer;
	}

}
