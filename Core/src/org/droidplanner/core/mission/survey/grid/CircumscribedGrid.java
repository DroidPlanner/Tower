package org.droidplanner.core.mission.survey.grid;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.coordinates.CoordBounds;
import org.droidplanner.core.helpers.geoTools.GeoTools;
import org.droidplanner.core.helpers.geoTools.LineCoord2D;

public class CircumscribedGrid {
	private static final int MAX_NUMBER_OF_LINES = 200;
	List<LineCoord2D> grid = new ArrayList<LineCoord2D>();
	private Coord2D gridLowerLeft;
	private double extrapolatedDiag;
	private Double angle;

	public CircumscribedGrid(List<Coord2D> polygonPoints, Double angle,
			Double lineDist) throws Exception {
		this.angle = angle;

		findPolygonBounds(polygonPoints);
		drawGrid(lineDist);
	}

	private void drawGrid(Double lineDist) throws GridWithTooManyLines {
		int lines = 0;
		Coord2D startPoint = gridLowerLeft;
		while (lines * lineDist < extrapolatedDiag) {
			Coord2D endPoint = GeoTools.newCoordFromBearingAndDistance(
					startPoint, angle, extrapolatedDiag);

			LineCoord2D line = new LineCoord2D(startPoint, endPoint);
			grid.add(line);

			startPoint = GeoTools.newCoordFromBearingAndDistance(startPoint,
					angle + 90, lineDist);
			lines++;
			if (lines > MAX_NUMBER_OF_LINES) {
				throw new GridWithTooManyLines();
			}
		}
	}

	private void findPolygonBounds(List<Coord2D> polygonPoints) {
		CoordBounds bounds = new CoordBounds(polygonPoints);
		Coord2D middlePoint = bounds.getMiddle();
		gridLowerLeft = GeoTools.newCoordFromBearingAndDistance(middlePoint,
				angle - 135, bounds.getDiag());
		extrapolatedDiag = bounds.getDiag() * 1.5;
	}

	public List<LineCoord2D> getGrid() {
		return grid;
	}

	public class GridWithTooManyLines extends Exception {
		private static final long serialVersionUID = 1L;
	}

}
