package com.droidplanner.fragments;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.droidplanner.DroidPlannerApp;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.fragments.helpers.OfflineMapFragment;
import com.droidplanner.fragments.markers.DroneMarker;
import com.droidplanner.fragments.markers.MarkerManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class FlightMapFragment extends OfflineMapFragment implements
		OnMapLongClickListener, OnMarkerClickListener {
	public interface OnDroneClickListner{
		public void onDroneClick();
	}
	
	public GoogleMap mMap;
	private Polyline flightPath;
	private Polyline missionPath;

	private int maxFlightPathSize;
	public boolean isAutoPanEnabled;
	private boolean isGuidedModeEnabled;

	private MarkerManager markers;

	public boolean hasBeenZoomed = false;

	public DroneMarker droneMarker;
	public Drone drone;
	private OnDroneClickListner listner;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);
		mMap = getMap();
		drone = ((DroidPlannerApp) getActivity().getApplication()).drone;

		markers = new MarkerManager(mMap);

		droneMarker = new DroneMarker(this);

		addFlightPathToMap();
		addMissionPathToMap();
		getPreferences();

		drone.setMapListner(droneMarker);
		mMap.setOnMapLongClickListener(this);
		mMap.setOnMarkerClickListener(this);

		return view;
	}

	private void getPreferences() {
		Context context = this.getActivity();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		maxFlightPathSize = Integer.valueOf(prefs.getString(
				"pref_max_fligth_path_size", "0"));
		isGuidedModeEnabled = prefs.getBoolean("pref_guided_mode_enabled",
				false);
		isAutoPanEnabled = prefs.getBoolean("pref_auto_pan_enabled", false);
	}

	public void updateMissionPath(Drone drone) {
		ArrayList<LatLng> missionPoints = new ArrayList<LatLng>();
		missionPoints.add(drone.mission.getHome().getCoord());
		for (waypoint point : drone.mission.getWaypoints()) {
			missionPoints.add(point.getCoord());
		}
		missionPath.setPoints(missionPoints);
	}

	public void addFlithPathPoint(LatLng position) {
		if (maxFlightPathSize > 0) {
			List<LatLng> oldFlightPath = flightPath.getPoints();
			if (oldFlightPath.size() > maxFlightPathSize) {
				oldFlightPath.remove(0);
			}
			oldFlightPath.add(position);
			flightPath.setPoints(oldFlightPath);
		}
	}

	public void clearFlightPath() {
		List<LatLng> oldFlightPath = flightPath.getPoints();
		oldFlightPath.clear();
		flightPath.setPoints(oldFlightPath);
	}

	public void zoomToLastKnowPosition() {
		if (drone.GPS.isPositionValid()) {
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
					drone.GPS.getPosition(), 16));
		} else {
			Toast.makeText(getActivity(),
					"There is no valid location for the Drone",
					Toast.LENGTH_SHORT).show();
		}
	}

	private void addMissionPathToMap() {
		PolylineOptions missionPathOptions = new PolylineOptions();
		missionPathOptions.color(Color.YELLOW).width(3).zIndex(0);
		missionPath = mMap.addPolyline(missionPathOptions);
	}

	private void addFlightPathToMap() {
		PolylineOptions flightPathOptions = new PolylineOptions();
		flightPathOptions.color(Color.argb(180, 0, 0, 200)).width(2).zIndex(1);
		flightPath = mMap.addPolyline(flightPathOptions);
	}

	@Override
	public void onMapLongClick(LatLng coord) {
		getPreferences();
		if (isGuidedModeEnabled) {
			drone.guidedPoint.newGuidedPoint(coord);
			markers.updateMarker(drone.guidedPoint, false);
		}
	}

	public void updateFragment() {
		updateMissionPath(drone);
		markers.updateMarker(drone.mission.getHome(), false);
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		if (droneMarker.equals(marker)) {
			listner.onDroneClick();
			return true;
		}
		return false;
	}

	public void setOnDroneClickListner(OnDroneClickListner listner) {
		this.listner = listner;
	}

}
