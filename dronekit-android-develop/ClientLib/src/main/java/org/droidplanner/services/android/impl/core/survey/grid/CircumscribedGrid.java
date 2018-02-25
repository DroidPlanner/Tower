package org.droidplanner.services.android.impl.core.survey.grid;

import org.droidplanner.services.android.impl.core.helpers.coordinates.CoordBounds;
import org.droidplanner.services.android.impl.core.helpers.geoTools.GeoTools;
import org.droidplanner.services.android.impl.core.helpers.geoTools.LineLatLong;
import com.o3dr.services.android.lib.coordinate.LatLong;

import java.util.ArrayList;
import java.util.List;

public class CircumscribedGrid {
	private static final int MAX_NUMBER_OF_LINES = 300;
	List<LineLatLong> grid = new ArrayList<LineLatLong>();
	private LatLong gridLowerLeft;
	private double extrapolatedDiag;
	private Double angle;

	public CircumscribedGrid(List<LatLong> polygonPoints, Double angle, Double lineDist)
			throws Exception {
		this.angle = angle;

		findPolygonBounds(polygonPoints);
		drawGrid(lineDist);
	}

	private void drawGrid(Double lineDist) throws GridWithTooManyLines {
		int lines = 0;
		LatLong startPoint = gridLowerLeft;
		while (lines * lineDist < extrapolatedDiag) {
			LatLong endPoint = GeoTools.newCoordFromBearingAndDistance(startPoint, angle,
					extrapolatedDiag);

			LineLatLong line = new LineLatLong(startPoint, endPoint);
			grid.add(line);

			startPoint = GeoTools.newCoordFromBearingAndDistance(startPoint, angle + 90, lineDist);
			lines++;
			if (lines > MAX_NUMBER_OF_LINES) {
				throw new GridWithTooManyLines();
			}
		}
	}

	private void findPolygonBounds(List<LatLong> polygonPoints) {
		CoordBounds bounds = new CoordBounds(polygonPoints);
		LatLong middlePoint = bounds.getMiddle();
		gridLowerLeft = GeoTools.newCoordFromBearingAndDistance(middlePoint, angle - 135,
				bounds.getDiag());
		extrapolatedDiag = bounds.getDiag() * 1.5;
	}

	public List<LineLatLong> getGrid() {
		return grid;
	}

	public class GridWithTooManyLines extends Exception {
		private static final long serialVersionUID = 1L;
	}

}
