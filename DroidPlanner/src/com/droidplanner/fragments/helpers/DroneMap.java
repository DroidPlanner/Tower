package com.droidplanner.fragments.helpers;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.droidplanner.DroidPlannerApp;
import com.droidplanner.drone.Drone;
import com.droidplanner.fragments.markers.MarkerManager;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

public class DroneMap extends OfflineMapFragment {

	public GoogleMap mMap;
	protected MarkerManager markers;
	protected MapPath missionPath;
	public Drone drone;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);
		mMap = getMap();
		markers = new MarkerManager(mMap);
		missionPath = new MapPath(mMap);
		drone = ((DroidPlannerApp) getActivity().getApplication()).drone;
		return view;
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