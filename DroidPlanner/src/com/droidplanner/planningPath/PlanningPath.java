package com.droidplanner.planningPath;

import java.util.List;

import android.graphics.Color;
import android.util.Log;

import com.droidplanner.polygon.Polygon;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class PlanningPath extends Polygon {

	private Polyline path;
	GoogleMap map;

	public PlanningPath(GoogleMap map) {
		this.map = map;
		initializePath();
	}

	public void addWaypoint(LatLng coord) {
		List<LatLng> pointList = path.getPoints();
		pointList.add(coord);
		path.setPoints(pointList);
	}

	public void clear() {
		path.remove();
		initializePath();
	}

	private void initializePath() {
		PolylineOptions polyline = new PolylineOptions();
		polyline.color(Color.RED).width(2);
		path = map.addPolyline(polyline);
	}

	public void finish() {
		Log.d("PATH", "END");
		
	}
}
