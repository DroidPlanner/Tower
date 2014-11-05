package org.droidplanner.android.utils;

import android.content.res.Resources;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.mapbox.mapboxsdk.api.ILatLng;
import com.ox3dr.services.android.lib.coordinate.LatLong;

public class DroneHelper {
	static public LatLng CoordToLatLang(LatLong coord) {
		return new LatLng(coord.getLatitude(), coord.getLongitude());
	}

	public static com.mapbox.mapboxsdk.geometry.LatLng CoordToLatLng(LatLong coord) {
		return new com.mapbox.mapboxsdk.geometry.LatLng(coord.getLatitude(), coord.getLongitude());
	}

	public static LatLong LatLngToCoord(LatLng point) {
		return new LatLong((float)point.latitude, (float) point.longitude);
	}

	public static LatLong ILatLngToCoord(ILatLng point) {
		return new LatLong((float) point.getLatitude(), (float) point.getLongitude());
	}

	public static LatLong LocationToCoord(Location location) {
		return new LatLong((float) location.getLatitude(), (float) location.getLongitude());
	}

	public static int scaleDpToPixels(double value, Resources res) {
		final float scale = res.getDisplayMetrics().density;
		return (int) Math.round(value * scale);
	}
}
