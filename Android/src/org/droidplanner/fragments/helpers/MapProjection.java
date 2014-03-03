package org.droidplanner.fragments.helpers;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.helpers.coordinates.Coord2D;

import android.graphics.Point;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;

public class MapProjection {
	
	public static List<LatLng> projectPathIntoMap(List<Coord2D> path,GoogleMap map) {
		List<LatLng> coords = new ArrayList<LatLng>();
		Projection projection = map.getProjection();
		
		for (Coord2D point : path) {
			coords.add(projection.fromScreenLocation(new Point((int) point.getX(),(int) point.getY())));
		}
		
		return coords;
	}
}
