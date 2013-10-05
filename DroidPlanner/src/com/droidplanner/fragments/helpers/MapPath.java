package com.droidplanner.fragments.helpers;

import java.util.List;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapPath {
	public interface PathSource {
		public List<LatLng> getPathPoints();
	}

	public Polyline missionPath;
	private GoogleMap mMap;
	private float width;
	private int color;

	public MapPath(GoogleMap mMap, int color, float width) {
		this.mMap = mMap;
		this.color = color;
		this.width = width;
	}

	public MapPath(GoogleMap mMap) {
		this(mMap, Color.YELLOW, 5);
	}

	public void update(PathSource pathSource) {
		addToMapIfNeeded();
		List<LatLng> newPath = pathSource.getPathPoints();
		missionPath.setPoints(newPath);
	}

	private void addToMapIfNeeded() {
		if (missionPath == null) {
			PolylineOptions flightPath = new PolylineOptions();
			flightPath.color(color).width(width);
			missionPath = mMap.addPolyline(flightPath);
		}
	}
}