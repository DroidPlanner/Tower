package org.droidplanner.fragments;

import java.util.List;

import org.droidplanner.dialogs.AlertDialogFragment;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.drone.variables.GuidedPoint;
import org.droidplanner.drone.variables.GuidedPoint.OnGuidedListener;
import org.droidplanner.fragments.helpers.DroneMap;
import org.droidplanner.fragments.helpers.MapPath;
import org.droidplanner.fragments.markers.DroneMarker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class FlightMapFragment extends DroneMap implements
		OnMapLongClickListener, OnMarkerClickListener, OnMarkerDragListener,
		OnGuidedListener {

	private static final int ZOOM_LEVEL = 20;

	private Polyline flightPath;
	private MapPath droneLeashPath;
	private int maxFlightPathSize;
	public boolean isAutoPanEnabled;

	public boolean hasBeenZoomed = false;

	public DroneMarker droneMarker;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);

		droneMarker = new DroneMarker(this);
		droneLeashPath = new MapPath(mMap, getResources());

		addFlightPathToMap();
		getPreferences();

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
		isAutoPanEnabled = prefs.getBoolean("pref_auto_pan_enabled", false);
	}

	@Override
	public void update() {
		super.update();
	}

	

	private void animateCamera(LatLng coord) {
		if (!hasBeenZoomed) {
			hasBeenZoomed = true;
			mMap.animateCamera(CameraUpdateFactory
					.newLatLngZoom(coord, ZOOM_LEVEL));
		}
		if (isAutoPanEnabled) {
			mMap.animateCamera(CameraUpdateFactory
					.newLatLngZoom(coord, ZOOM_LEVEL));
		}
	}

	public void addFlightPathPoint(LatLng position) {
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
		flightPathOptions.color(0xfffd693f).width(6).zIndex(1);
		flightPath = mMap.addPolyline(flightPathOptions);
	}

	@Override
	public void onMapLongClick(LatLng coord) {

		FragmentTransaction fragManager = getFragmentManager()
				.beginTransaction();

		// this is a piece to check if we are not displayed - do not need it
		// here in my opinion

		/*
		 * Fragment prev = getFragmentManager().findFragmentByTag("warn"); if
		 * (prev != null) { fragManager.remove(prev); }
		 * 
		 * // and back key action fragManager.addToBackStack(null);
		 */

		// we have a lot of possible parameters for this dialog ...
		// activity context, style mode,title, message,action be executed
		// code, and it's params (have to be parcelable class)
		DialogFragment warnGuidedDialog = AlertDialogFragment.newInstance(
				context, 7, "Sample Title", "Do not go Guided !!!", 0, coord);
		warnGuidedDialog.setCancelable(false);
		warnGuidedDialog.show(fragManager, "warn");

	}

	@Override
	public void onMarkerDragStart(Marker marker) {
	}

	@Override
	public void onMarkerDrag(Marker marker) {
	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		drone.guidedPoint.changeGuidedCoordinate(marker.getPosition());
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		drone.guidedPoint.changeGuidedCoordinate(marker.getPosition());
		return true;
	}

	@Override
	public void onGuidedPoint() {
		GuidedPoint guidedPoint = drone.guidedPoint;
		markers.updateMarker(guidedPoint, true, context);
		droneLeashPath.update(guidedPoint);
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case GPS:
			droneLeashPath.update(drone.guidedPoint);
			addFlightPathPoint(drone.GPS
					.getPosition());
			animateCamera(drone.GPS.getPosition());
			break;
		default:
			break;
		}
		super.onDroneEvent(event, drone);
	}
}
