package com.droidplanner.fragments.helpers;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Point;

import com.droidplanner.drone.variables.waypoint;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;

public class MapProjection {
	
	public static List<waypoint> projectPathIntoMap(List<Point> path,GoogleMap map,double altitude) {
		List<waypoint> waypoints = new ArrayList<waypoint>();
		Projection projection = map.getProjection();
		
		for (Point point : path) {
			LatLng coord = projection.fromScreenLocation(point);
			waypoints.add(new waypoint(coord, altitude));
		}
		
		return waypoints;
	}
}
