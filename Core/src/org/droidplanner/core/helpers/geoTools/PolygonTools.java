package org.droidplanner.core.helpers.geoTools;

import java.util.ArrayList;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.polygon.Polygon;

public class PolygonTools {

	public static Polygon offsetPolygon(Polygon polygon) {
		ArrayList<LineCoord2D> offsetLines = new ArrayList<LineCoord2D>();		
		for (LineCoord2D line : polygon.getLines()) {
			offsetLines.add(LineTools.getParallelLineToTheLeft(line,10));
		}
		
		ArrayList<Coord2D> path = new ArrayList<Coord2D>();
		offsetLines.add(offsetLines.get(0));
		offsetLines.add(offsetLines.get(1));
		for (int i = 1; i < offsetLines.size(); i++) {
			try {
				path.add(LineTools.FindLineIntersection(offsetLines.get(i-1), offsetLines.get(i),true));
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
		Polygon polygonwithOffset = new Polygon();
		polygonwithOffset.addPoints(path);
		return polygonwithOffset;
	}

}
