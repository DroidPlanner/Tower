package org.droidplanner.services.android.impl.core.helpers.geoTools;

import org.droidplanner.services.android.impl.core.helpers.coordinates.CoordBounds;
import com.o3dr.services.android.lib.coordinate.LatLong;

import java.util.ArrayList;
import java.util.List;

public class LineTools {

	public static LineLatLong findExternalPoints(ArrayList<LatLong> crosses) {
		LatLong meanCoord = new CoordBounds(crosses).getMiddle();
		LatLong start = PointTools.findFarthestPoint(crosses, meanCoord);
		LatLong end = PointTools.findFarthestPoint(crosses, start);
		return new LineLatLong(start, end);
	}

	/**
	 * Finds the intersection of two lines http://stackoverflow.com/questions/
	 * 1119451/how-to-tell-if-a-line-intersects -a-polygon-in-c
	 */
	public static LatLong FindLineIntersection(LineLatLong first, LineLatLong second) {
		double denom = ((first.getEnd().getLatitude() - first.getStart().getLatitude()) * (second.getEnd().getLongitude() - second
				.getStart().getLongitude()))
				- ((first.getEnd().getLongitude() - first.getStart().getLongitude()) * (second.getEnd().getLatitude() - second
						.getStart().getLatitude()));
		if (denom == 0){
            //Parallel lines
            return null;
        }
		double numer = ((first.getStart().getLongitude() - second.getStart().getLongitude()) * (second.getEnd()
				.getLatitude() - second.getStart().getLatitude()))
				- ((first.getStart().getLatitude() - second.getStart().getLatitude()) * (second.getEnd().getLongitude() - second
						.getStart().getLongitude()));
		double r = numer / denom;
		double numer2 = ((first.getStart().getLongitude() - second.getStart().getLongitude()) * (first.getEnd()
				.getLatitude() - first.getStart().getLatitude()))
				- ((first.getStart().getLatitude() - second.getStart().getLatitude()) * (first.getEnd().getLongitude() - first
						.getStart().getLongitude()));
		double s = numer2 / denom;
		if ((r < 0 || r > 1) || (s < 0 || s > 1)){
            //No intersection
            return null;
        }
		// Find intersection point
		double x = first.getStart().getLatitude()
				+ (r * (first.getEnd().getLatitude() - first.getStart().getLatitude()));
		double y = first.getStart().getLongitude()
				+ (r * (first.getEnd().getLongitude() - first.getStart().getLongitude()));
		return (new LatLong(x, y));
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
	public static LineLatLong findClosestLineToPoint(LatLong point, List<LineLatLong> list) {
		LineLatLong answer = list.get(0);
		double shortest = Double.MAX_VALUE;

		for (LineLatLong line : list) {
			double ans1 = GeoTools.getAproximatedDistance(point, line.getStart());
			double ans2 = GeoTools.getAproximatedDistance(point, line.getEnd());
			LatLong shorterpnt = ans1 < ans2 ? line.getStart() : line.getEnd();

			if (shortest > GeoTools.getAproximatedDistance(point, shorterpnt)) {
				answer = line;
				shortest = GeoTools.getAproximatedDistance(point, shorterpnt);
			}
		}
		return answer;
	}

}
