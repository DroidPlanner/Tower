package com.droidplanner.fragments.helpers;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.droidplanner.DroidPlannerApp;
import com.droidplanner.DroidPlannerApp.OnWaypointChangedListner;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.variables.Home;
import com.droidplanner.drone.variables.mission.Mission;
import com.droidplanner.fragments.markers.MarkerManager;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

public abstract class DroneMap extends OfflineMapFragment implements OnWaypointChangedListner {
	public GoogleMap mMap;
	protected MarkerManager markers;
	protected MapPath missionPath;
	public Drone drone;
	public Mission mission;
	protected Context context;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);
		drone = ((DroidPlannerApp) getActivity().getApplication()).drone;		
		mission = drone.mission;
		mMap = getMap();
		markers = new MarkerManager(mMap);
		missionPath = new MapPath(mMap);
		mission.addOnMissionUpdateListner(this);
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = activity.getApplicationContext();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mission.removeOnMissionUpdateListner(this);
	}

	public LatLng getMyLocation() {
		if (mMap.getMyLocation() != null) {
			return new LatLng(mMap.getMyLocation().getLatitude(), mMap
					.getMyLocation().getLongitude());
		} else {
			return null;
		}
	}

	public void update() {
		markers.clean();
		
		Home home = drone.home.getHome();
		if (home.isValid()) {
			markers.updateMarker(home, false, context);			
		}
		
		markers.updateMarkers(mission.getMarkers(), true, context);
		
		missionPath.update(mission);
	}

	@Override
	public void onMissionUpdate() {
		update();
	}

}