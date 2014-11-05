package org.droidplanner.android.utils;

import android.content.res.Resources;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.mapbox.mapboxsdk.api.ILatLng;

public class DroneHelper {
	static public LatLng CoordToLatLang(com.ox3dr.services.android.lib.coordinate.LatLng coord) {
		return new LatLng(coord.getLatitude(), coord.getLongitude());
	}

	public static com.mapbox.mapboxsdk.geometry.LatLng CoordToLatLng(com.ox3dr.services.android.lib.coordinate.LatLng coord) {
		return new com.mapbox.mapboxsdk.geometry.LatLng(coord.getLatitude(), coord.getLongitude());
	}

	public static com.ox3dr.services.android.lib.coordinate.LatLng LatLngToCoord(LatLng point) {
		return new com.ox3dr.services.android.lib.coordinate.LatLng((float)point.latitude,
                (float) point.longitude);
	}

	public static com.ox3dr.services.android.lib.coordinate.LatLng ILatLngToCoord(ILatLng point) {
		return new com.ox3dr.services.android.lib.coordinate.LatLng((float) point.getLatitude(),
                (float) point.getLongitude());
	}

	public static com.ox3dr.services.android.lib.coordinate.LatLng LocationToCoord(Location location) {
		return new com.ox3dr.services.android.lib.coordinate.LatLng((float) location.getLatitude(),
                (float) location.getLongitude());
	}

	public static int scaleDpToPixels(double value, Resources res) {
		final float scale = res.getDisplayMetrics().density;
		return (int) Math.round(value * scale);
	}
}
