package com.droidplanner.fragments.helpers;

import java.util.List;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapPath {
	public interface PathSource{
		public List<LatLng> getPathPoints();
	}
	
	public Polyline missionPath;
	private GoogleMap mMap;

	public MapPath(GoogleMap mMap) {
		this.mMap = mMap;
	}

	public void update(PathSource pathSource) {
		addToMapIfNeeded();
		List<LatLng> newPath = pathSource.getPathPoints();
		missionPath.setPoints(newPath);
	}

	private void addToMapIfNeeded() {
		if (missionPath == null) {
			PolylineOptions flightPath = new PolylineOptions();
			flightPath.color(Color.YELLOW).width(3);
			missionPath = mMap.addPolyline(flightPath);
		}
	}
}