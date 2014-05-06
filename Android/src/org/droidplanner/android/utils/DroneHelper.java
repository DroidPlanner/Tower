package org.droidplanner.android.utils;

import android.content.res.Resources;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

import com.google.android.gms.maps.model.LatLng;

public class DroneHelper {
	static public LatLng CoordToLatLang(Coord2D coord) {
		return new LatLng(coord.getLat(), coord.getLng());
	}

    public static GeoPoint CoordToGeoPoint(Coord2D coord){
        return new GeoPoint(coord.getLat(), coord.getLng());
    }

    public static Coord2D GeoPointToCoord(IGeoPoint point){
        return new Coord2D(point.getLatitude(), point.getLongitude());
    }

	public static Coord2D LatLngToCoord(LatLng point) {
		return new Coord2D(point.latitude, point.longitude);
	}

    public static int scaleDpToPixels(double value, Resources res) {
        final float scale = res.getDisplayMetrics().density;
        return (int) Math.round(value * scale);
    }
}
