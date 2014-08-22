package org.droidplanner.android.utils;

import org.droidplanner.core.helpers.coordinates.Coord2D;

import android.content.res.Resources;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.mapbox.mapboxsdk.api.ILatLng;

public class DroneHelper {
	static public LatLng CoordToLatLang(Coord2D coord) {
		return new LatLng(coord.getLat(), coord.getLng());
	}

	public static com.mapbox.mapboxsdk.geometry.LatLng CoordToLatLng(Coord2D coord) {
		return new com.mapbox.mapboxsdk.geometry.LatLng(coord.getLat(), coord.getLng());
	}

	public static Coord2D LatLngToCoord(LatLng point) {
		return new Coord2D(point.latitude, point.longitude);
	}

	public static Coord2D ILatLngToCoord(ILatLng point) {
		return new Coord2D(point.getLatitude(), point.getLongitude());
	}

	public static Coord2D LocationToCoord(Location location) {
		return new Coord2D(location.getLatitude(), location.getLongitude());
	}

	public static int scaleDpToPixels(double value, Resources res) {
		final float scale = res.getDisplayMetrics().density;
		return (int) Math.round(value * scale);
	}
}
