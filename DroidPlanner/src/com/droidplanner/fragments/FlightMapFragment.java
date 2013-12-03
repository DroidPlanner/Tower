package com.droidplanner.fragments;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.droidplanner.drone.DroneInterfaces.MapUpdatedListner;
import com.droidplanner.drone.variables.GuidedPoint;
import com.droidplanner.drone.variables.GuidedPoint.OnGuidedListener;
import com.droidplanner.fragments.helpers.DroneMap;
import com.droidplanner.fragments.helpers.MapPath;
import com.droidplanner.fragments.markers.DroneMarker;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class FlightMapFragment extends DroneMap implements
		OnMapLongClickListener, OnMarkerClickListener, OnMarkerDragListener, OnGuidedListener, MapUpdatedListner {
	private Polyline flightPath;
	private MapPath droneLeashPath;
	private int maxFlightPathSize;
	public boolean isAutoPanEnabled;
	private boolean isGuidedModeEnabled;

	public boolean hasBeenZoomed = false;

	public DroneMarker droneMarker;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);

		droneMarker = new DroneMarker(this);
		droneLeashPath = new MapPath(mMap,getResources());

		addFlightPathToMap();
		getPreferences();


		drone.setMapListner(this);
		mMap.setOnMapLongClickListener(this);
		mMap.setOnMarkerDragListener(this);
		mMap.setOnMarkerClickListener(this);
		
		drone.setGuidedPointListner(this);
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

	@Override
	public void update() {
		super.update();
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

	private void addFlightPathToMap() {
		PolylineOptions flightPathOptions = new PolylineOptions();
		flightPathOptions.color(Color.argb(180, 0, 0, 200)).width(2).zIndex(1);
		flightPath = mMap.addPolyline(flightPathOptions);
	}

	@Override
	public void onMapLongClick(LatLng coord) {
		getPreferences();
		if (isGuidedModeEnabled)
			drone.guidedPoint.newGuidedPointWithCurrentAlt(coord);
	}

	@Override
	public void onMarkerDragStart(Marker marker){
	}

	@Override
	public void onMarkerDrag(Marker marker){
	}

	
	@Override
	public void onMarkerDragEnd(Marker marker){
		drone.guidedPoint.newGuidedPointwithLastAltitude(marker.getPosition());
	}

	@Override
	public boolean onMarkerClick(Marker marker){
		drone.guidedPoint.newGuidedPointWithCurrentAlt(marker.getPosition());
		return true;
	}

	@Override
	public void onGuidedPoint() {
		GuidedPoint guidedPoint = drone.guidedPoint;
		markers.updateMarker(guidedPoint, true, context);
		droneLeashPath.update(guidedPoint);
	}

	@Override
	public void onDroneUpdate() {
		droneMarker.onDroneUpdate();
		droneLeashPath.update(drone.guidedPoint);
	}

	@Override
	public void onDroneTypeChanged() {
		droneMarker.onDroneTypeChanged();
	}
}
