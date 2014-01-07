package com.droidplanner.drone.variables.mission.survey.grid;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import com.droidplanner.R;

import com.droidplanner.helpers.geoTools.GeoTools;
import com.droidplanner.helpers.geoTools.LineLatLng;
import com.droidplanner.polygon.PolyBounds;
import com.google.android.gms.maps.model.LatLng;

public class CircumscribedGrid {
	private static final int MAX_NUMBER_OF_LINES = 200;
	List<LineLatLng> grid = new ArrayList<LineLatLng>();
	private LatLng gridLowerLeft;
	private double extrapolatedDiag;
	private Double angle;
	private Context currContext;

	public CircumscribedGrid(List<LatLng> polygonPoints, Double angle,
			Double lineDist,Context usedContext) throws Exception {
		this.angle = angle;
		this.currContext = usedContext;

		findPolygonBounds(polygonPoints);
		drawGrid(lineDist);
	}

	private void drawGrid(Double lineDist) throws Exception {
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
				throw new Exception(currContext.getString(R.string.mission_is_too_lengthy));				
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

}
