package com.droidplanner.planningPath;

import java.util.List;

import android.graphics.Color;

import com.droidplanner.polygon.Polygon;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class PlanningPath extends Polygon {

	private Polyline path;

	public PlanningPath(GoogleMap map) {
		PolylineOptions polyline = new PolylineOptions();
		polyline.color(Color.RED).width(1);
		path = map.addPolyline(polyline);
	}

	public void addWaypoint(LatLng coord) {
		List<LatLng> pointList = path.getPoints();
		pointList.add(coord);				
		path.setPoints(pointList);
	}
}
