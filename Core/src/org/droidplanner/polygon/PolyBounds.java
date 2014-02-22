package org.droidplanner.polygon;

import java.util.List;

import org.droidplanner.helpers.coordinates.Coord2D;
import org.droidplanner.helpers.geoTools.GeoTools;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * 
 * Object for holding boundary for a polygon
 * 
 */
public class PolyBounds {
	public Coord2D sw;
	public Coord2D ne;

	public PolyBounds(List<Coord2D> points) {
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		for (Coord2D point : points) {
			builder.include(point);
		}
		LatLngBounds bounds = builder.build();
		sw = bounds.southwest;
		ne = bounds.northeast;
	}

	public double getDiag() {
		return GeoTools.latToMeters(GeoTools.getAproximatedDistance(ne, sw));
	}

	public Coord2D getMiddle() {
		return (new Coord2D((ne.getY() + sw.getY()) / 2,
				(ne.getX() + sw.getX()) / 2));

	}
}
