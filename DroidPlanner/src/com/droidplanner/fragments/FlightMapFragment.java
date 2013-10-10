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
import android.widget.Toast;

import com.MAVLink.Messages.ApmModes;
import com.droidplanner.DroidPlannerApp;
import com.droidplanner.drone.Drone;
import com.droidplanner.fragments.helpers.DroneMap;
import com.droidplanner.fragments.helpers.GuidePointListener;
import com.droidplanner.fragments.markers.DroneMarker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class FlightMapFragment extends DroneMap implements
		OnMapLongClickListener, OnMarkerClickListener, OnMarkerDragListener {
	private Polyline flightPath;
	private int maxFlightPathSize;
	public boolean isAutoPanEnabled;
	private boolean isGuidedModeEnabled;

	public boolean hasBeenZoomed = false;

	public DroneMarker droneMarker;
	public Drone drone;

	private GuidePointListener guidePointListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);
		drone = ((DroidPlannerApp) getActivity().getApplication()).drone;

		droneMarker = new DroneMarker(this);

		addFlightPathToMap();
		getPreferences();

		drone.setMapListner(droneMarker);
		mMap.setOnMapLongClickListener(this);
		mMap.setOnMarkerDragListener(this);
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

	private void addFlightPathToMap() {
		PolylineOptions flightPathOptions = new PolylineOptions();
		flightPathOptions.color(Color.argb(180, 0, 0, 200)).width(2).zIndex(1);
		flightPath = mMap.addPolyline(flightPathOptions);
	}

	public void setGuidePointListener(GuidePointListener guidePointListener) {
		this.guidePointListener = guidePointListener;
	}

	@Override
	public void onMapLongClick(LatLng coord) {
		getPreferences();
		if (isGuidedModeEnabled)
			drone.guidedPoint.newGuidedPoint(coord);
	}

	@Override
	public void onMarkerDragStart(Marker marker)
	{
		checkForGuidePointMoving(marker);
	}

	@Override
	public void onMarkerDrag(Marker marker)
	{
		checkForGuidePointMoving(marker);
	}

	@Override
	public void onMarkerDragEnd(Marker marker)
	{
		checkForGuidePointMoving(marker);
		drone.guidedPoint.newGuidedPoint(marker.getPosition());
	}

	@Override
	public boolean onMarkerClick(Marker marker)
	{
		drone.guidedPoint.newGuidedPoint(marker.getPosition());
		return true;
	}

	private void checkForGuidePointMoving(Marker marker)
	{
		final Context context = getActivity().getApplicationContext();
		drone.guidedPoint.setCoord(marker.getPosition());
		guidePointListener.OnGuidePointMoved();
	}

	public void updateFragment() {
		final Context context = getActivity().getApplicationContext();

		missionPath.update(drone.mission);
		markers.updateMarker(drone.mission.getHome(), false, context);

		if(drone.guidedPoint.isCoordValid()) {
			markers.updateMarker(drone.guidedPoint, true, context);
			guidePointListener.OnGuidePointMoved();
		}
	}

	public void onModeChanged() {
		if(drone.state.getMode() != ApmModes.ROTOR_GUIDED) {
			if(drone.guidedPoint.isCoordValid()) {
				markers.clear();
				drone.guidedPoint.invalidateCoord();
			}
		}
	}
}
