package com.droidplanner.fragments.helpers;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Point;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;

public class MapProjection {
	
	public static List<LatLng> projectPathIntoMap(List<Point> path,GoogleMap map) {
		List<LatLng> coords = new ArrayList<LatLng>();
		Projection projection = map.getProjection();
		
		for (Point point : path) {
			coords.add(projection.fromScreenLocation(point));
		}
		
		return coords;
	}
}
