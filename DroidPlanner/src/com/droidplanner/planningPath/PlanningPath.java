package com.droidplanner.planningPath;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;

import com.droidplanner.drone.variables.waypoint;
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

	public List<waypoint> getWaypoints(double altitude) {
		List<LatLng> pointList = path.getPoints();
		
		pointList = Simplify.simplify(pointList, 2.0);
		
		List<waypoint> waypoints = new ArrayList<waypoint>();
		for (LatLng point : pointList) {
			waypoints.add(new waypoint(point, altitude));
		}
		return waypoints;
	}
}
