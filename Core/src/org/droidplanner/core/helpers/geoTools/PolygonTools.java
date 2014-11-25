package org.droidplanner.core.helpers.geoTools;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.polygon.Polygon;

public class PolygonTools {

	public static Polygon offsetPolygon(Polygon polygon) throws Exception {
		if (!PolygonTools.isSimplePolygon(polygon)) {
			throw new Exception("Complex Polygon");
		}

		ArrayList<LineCoord2D> offsetLines = new ArrayList<LineCoord2D>();
		for (LineCoord2D line : polygon.getLines()) {
			offsetLines.add(LineTools.getParallelLineToTheLeft(line, 10));
		}

		ArrayList<Coord2D> path = new ArrayList<Coord2D>();
		offsetLines.add(offsetLines.get(0));
		offsetLines.add(offsetLines.get(1));
		for (int i = 1; i < offsetLines.size(); i++) {
			try {
				path.add(LineTools.FindLineIntersection(offsetLines.get(i - 1), offsetLines.get(i),
						true));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Polygon polygonwithOffset = new Polygon();
		polygonwithOffset.addPoints(path);
		return polygonwithOffset;
	}

	public static boolean isSimplePolygon(Polygon polygon) {
		List<LineCoord2D> lines = polygon.getLines();
		for (LineCoord2D line1 : lines) {
			for (LineCoord2D line2 : lines) {
				if (line1.equals(line2)) {
					continue;
				}
				try{
					Coord2D intersection = LineTools.FindLineIntersection(line1, line2);
					if (intersection.equals(line1.getStart())
							|| intersection.equals(line1.getEnd())
							|| intersection.equals(line2.getStart())
							|| intersection.equals(line2.getEnd())) {
						continue;
					}
					return false;
				} catch (Exception e) {
				}
			}
		}
		return true;
	}

	public static boolean isClockWisePolygon(Polygon polygon) {
		double sum = 0;
		for (LineCoord2D line : polygon.getLines()) {
			sum += (line.getEnd().getX() - line.getStart().getX())
					/ (line.getEnd().getY() + line.getStart().getY());
		}
		return sum > 0 ? false : true;
	}
}
