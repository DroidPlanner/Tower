package com.droidplanner.fragments.helpers;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;

import com.droidplanner.drone.Drone;
import com.droidplanner.drone.variables.waypoint;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapPath {
	public Polyline missionPath;
	private GoogleMap mMap;

	public MapPath(GoogleMap mMap) {
		this.mMap = mMap;
	}

	public void updateMissionPath(Drone drone) {
		addToMapIfNeeded();
		List<LatLng> newPath = getPathPoints(drone);
		missionPath.setPoints(newPath);
	}

	private void addToMapIfNeeded() {
		if (missionPath == null) {
			PolylineOptions flightPath = new PolylineOptions();
			flightPath.color(Color.YELLOW).width(3);
			missionPath = mMap.addPolyline(flightPath);
		}
	}

	private List<LatLng> getPathPoints(Drone drone) {
		List<LatLng> newPath = new ArrayList<LatLng>();
		newPath.add(drone.mission.getHome().getCoord());
		for (waypoint point : drone.mission.getWaypoints()) {
			newPath.add(point.getCoord());
		}
		return newPath;
	}
}