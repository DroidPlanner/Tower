package com.droidplanner.survey.grid;

import java.util.ArrayList;
import java.util.List;

import com.droidplanner.helpers.geoTools.GeoTools;
import com.droidplanner.polygon.PolyBounds;
import com.google.android.gms.maps.model.LatLng;

public class GridGenerator {
	List<LineLatLng> grid = new ArrayList<LineLatLng>();
	
	public GridGenerator(List<LatLng> waypoints2,
			Double angle, Double lineDist) {		
	
		PolyBounds bounds = new PolyBounds(waypoints2);
		LatLng point = new LatLng(bounds.getMiddle().latitude,
				bounds.getMiddle().longitude);
	
		point = GeoTools.newCoordFromBearingAndDistance(point, angle - 135, bounds.getDiag());
	
		// get x y step amount in lat lng from m
		Double y1 = Math.cos(Math.toRadians(angle + 90));
		Double x1 = Math.sin(Math.toRadians(angle + 90));
		LatLng diff = new LatLng(GeoTools.metersTolat(lineDist * y1),
				GeoTools.metersTolat(lineDist * x1));
	
		// draw grid
		int lines = 0;
		while (lines * lineDist < bounds.getDiag() * 1.5) {
			LatLng pointx = point;
			pointx = GeoTools.newCoordFromBearingAndDistance(pointx, angle, bounds.getDiag() * 1.5);
	
			LineLatLng line = new LineLatLng(point, pointx);
			grid.add(line);
	
			point = GeoTools.addLatLng(point, diff);
			lines++;
		}
	}

	public List<LineLatLng> getGrid() {
		return grid;
	}

}
