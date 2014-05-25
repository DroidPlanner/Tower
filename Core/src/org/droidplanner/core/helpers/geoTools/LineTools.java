package org.droidplanner.core.helpers.geoTools;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.coordinates.CoordBounds;

public class LineTools {

	public static LineCoord2D findExternalPoints(ArrayList<Coord2D> crosses) {
		Coord2D meanCoord = new CoordBounds(crosses).getMiddle();
		Coord2D start = PointTools.findFarthestPoint(crosses, meanCoord);
		Coord2D end = PointTools.findFarthestPoint(crosses, start);
		return new LineCoord2D(start, end);
	}

	/**
	 * Finds the intersection of two lines http://stackoverflow.com/questions/
	 * 1119451/how-to-tell-if-a-line-intersects -a-polygon-in-c
	 * 
	 * @throws Exception
	 */
	public static Coord2D FindLineIntersection(LineCoord2D first,
			LineCoord2D second) throws Exception {
		double denom = ((first.getEnd().getX() - first.getStart().getX()) * (second.getEnd().getY() - second.getStart()
				.getY()))
				- ((first.getEnd().getY() - first.getStart().getY()) * (second.getEnd().getX() - second.getStart()
						.getX()));
		if (denom == 0)
			throw new Exception("Parralel Lines");
		double numer = ((first.getStart().getY() - second.getStart().getY()) * (second.getEnd()
				.getX() - second.getStart().getX()))
				- ((first.getStart().getX() - second.getStart().getX()) * (second.getEnd().getY() - second.getStart()
						.getY()));
		double r = numer / denom;
		double numer2 = ((first.getStart().getY() - second.getStart().getY()) * (first.getEnd()
				.getX() - first.getStart().getX()))
				- ((first.getStart().getX() - second.getStart().getX()) * (first.getEnd().getY() - first.getStart()
						.getY()));
		double s = numer2 / denom;
		if ((r < 0 || r > 1) || (s < 0 || s > 1))
			throw new Exception("No Intersection");
		// Find intersection point
		double x = first.getStart().getX() + (r * (first.getEnd().getX() - first.getStart().getX()));
		double y = first.getStart().getY() + (r * (first.getEnd().getY() - first.getStart().getY()));
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
	public static LineCoord2D findClosestLineToPoint(Coord2D point,
			List<LineCoord2D> list) {
		LineCoord2D answer = list.get(0);
		double shortest = Double.MAX_VALUE;

		for (LineCoord2D line : list) {
			double ans1 = GeoTools.getAproximatedDistance(point, line.getStart());
			double ans2 = GeoTools.getAproximatedDistance(point, line.getEnd());
			Coord2D shorterpnt = ans1 < ans2 ? line.getStart() : line.getEnd();

			if (shortest > GeoTools.getAproximatedDistance(point, shorterpnt)) {
				answer = line;
				shortest = GeoTools.getAproximatedDistance(point, shorterpnt);
			}
		}
		return answer;
	}

}
