package org.droidplanner.drone.variables.mission.survey.grid;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.helpers.geoTools.GeoTools;
import org.droidplanner.helpers.geoTools.LineLatLng;
import org.droidplanner.polygon.PolyBounds;

import com.google.android.gms.maps.model.LatLng;

public class CircumscribedGrid {
	private static final int MAX_NUMBER_OF_LINES = 200;
	List<LineLatLng> grid = new ArrayList<LineLatLng>();
	private LatLng gridLowerLeft;
	private double extrapolatedDiag;
	private Double angle;
	
	public CircumscribedGrid(List<LatLng> polygonPoints, Double angle,
			Double lineDist) throws Exception {
		this.angle = angle;

		findPolygonBounds(polygonPoints);
		drawGrid(lineDist);
	}

	private void drawGrid(Double lineDist) throws GridWithTooManyLines  {
		int lines = 0;
		LatLng startPoint = gridLowerLeft;
		while (lines * lineDist < extrapolatedDiag) {
			LatLng endPoint = GeoTools.newCoordFromBearingAndDistance(
					startPoint, angle, extrapolatedDiag);

			LineLatLng line = new LineLatLng(startPoint, endPoint);
			grid.add(line);

			startPoint = GeoTools.newCoordFromBearingAndDistance(startPoint,
					angle + 90, lineDist);
			lines++;
			if (lines>MAX_NUMBER_OF_LINES) {
				throw new GridWithTooManyLines();				
			}
		}
	}

	private void findPolygonBounds(List<LatLng> polygonPoints) {
		PolyBounds bounds = new PolyBounds(polygonPoints);
		LatLng middlePoint = bounds.getMiddle();
		gridLowerLeft = GeoTools.newCoordFromBearingAndDistance(middlePoint,
				angle - 135, bounds.getDiag());
		extrapolatedDiag = bounds.getDiag() * 1.5;
	}

	public List<LineLatLng> getGrid() {
		return grid;
	}

	public class GridWithTooManyLines extends Exception {
		private static final long serialVersionUID = 1L;	
	}

}
