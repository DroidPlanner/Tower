package com.droidplanner.fragments.helpers;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.droidplanner.drone.Drone;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.fragments.markers.MarkerManager;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class DroneMap extends OfflineMapFragment {

	public GoogleMap mMap;
	protected MarkerManager markers;
	protected Polyline missionPath;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);
		mMap = getMap();
		markers = new MarkerManager(mMap);
		return view;
	}

	protected void updateMissionPath(Drone drone) {
		if (missionPath != null) {
			missionPath.remove();
		}

		PolylineOptions flightPath = new PolylineOptions();
		flightPath.color(Color.YELLOW).width(3);

		flightPath.add(drone.mission.getHome().getCoord());
		for (waypoint point : drone.mission.getWaypoints()) {
			flightPath.add(point.getCoord());
		}
		PolylineOptions missionPath2 = flightPath;
		missionPath = mMap.addPolyline(missionPath2);
	}

	public LatLng getMyLocation() {
		if (mMap.getMyLocation() != null) {
			return new LatLng(mMap.getMyLocation().getLatitude(), mMap
					.getMyLocation().getLongitude());
		} else {
			return null;
		}
	}

}