package org.droidplanner.core.helpers.geoTools;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.coordinates.CoordBounds;

public class LineTools {

	public static LineLatLng findExternalPoints(ArrayList<Coord2D> crosses) {
		Coord2D meanCoord = new CoordBounds(crosses).getMiddle();
		Coord2D start = PointTools.findFarthestPoint(crosses, meanCoord);
		Coord2D end = PointTools.findFarthestPoint(crosses, start);
		return new LineLatLng(start, end);
	}

	/**
	 * Finds the intersection of two lines http://stackoverflow.com/questions/
	 * 1119451/how-to-tell-if-a-line-intersects -a-polygon-in-c
	 * 
	 * @throws Exception
	 */
	public static Coord2D FindLineIntersection(LineLatLng first,
			LineLatLng second) throws Exception {
		double denom = ((first.p2.getX() - first.p1.getX()) * (second.p2.getY() - second.p1
				.getY()))
				- ((first.p2.getY() - first.p1.getY()) * (second.p2.getX() - second.p1
						.getX()));
		if (denom == 0)
			throw new Exception("Parralel Lines");
		double numer = ((first.p1.getY() - second.p1.getY()) * (second.p2
				.getX() - second.p1.getX()))
				- ((first.p1.getX() - second.p1.getX()) * (second.p2.getY() - second.p1
						.getY()));
		double r = numer / denom;
		double numer2 = ((first.p1.getY() - second.p1.getY()) * (first.p2
				.getX() - first.p1.getX()))
				- ((first.p1.getX() - second.p1.getX()) * (first.p2.getY() - first.p1
						.getY()));
		double s = numer2 / denom;
		if ((r < 0 || r > 1) || (s < 0 || s > 1))
			throw new Exception("No Intersection");
		// Find intersection point
		double x = first.p1.getX() + (r * (first.p2.getX() - first.p1.getX()));
		double y = first.p1.getY() + (r * (first.p2.getY() - first.p1.getY()));
		return (new Coord2D(x, y));
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
	public static LineLatLng findClosestLineToPoint(Coord2D point,
			List<LineLatLng> list) {
		LineLatLng answer = list.get(0);
		double shortest = Double.MAX_VALUE;

		for (LineLatLng line : list) {
			double ans1 = GeoTools.getAproximatedDistance(point, line.p1);
			double ans2 = GeoTools.getAproximatedDistance(point, line.p2);
			Coord2D shorterpnt = ans1 < ans2 ? line.p1 : line.p2;

			if (shortest > GeoTools.getAproximatedDistance(point, shorterpnt)) {
				answer = line;
				shortest = GeoTools.getAproximatedDistance(point, shorterpnt);
			}
		}
		return answer;
	}

}
