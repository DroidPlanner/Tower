package org.droidplanner.android.fragments.helpers;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord2D;

import android.graphics.Point;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;

public class MapProjection {

	public static List<Coord2D> projectPathIntoMap(List<Coord2D> path,
			GoogleMap map) {
		List<Coord2D> coords = new ArrayList<Coord2D>();
		Projection projection = map.getProjection();

		for (Coord2D point : path) {
			LatLng coord = projection.fromScreenLocation(new Point((int) point
					.getX(), (int) point.getY()));
			coords.add(new Coord2D(coord.latitude, coord.longitude));
		}

		return coords;
	}
}
