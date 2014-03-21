package org.droidplanner.fragments;

import java.util.List;

import org.droidplanner.dialogs.GuidedDialog;
import org.droidplanner.dialogs.GuidedDialog.GuidedDialogListener;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.fragments.helpers.DroneMap;
import org.droidplanner.fragments.helpers.MapPath;
import org.droidplanner.graphic.DroneHelper;
import org.droidplanner.graphic.GraphicDrone;
import org.droidplanner.graphic.GraphicGuided;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
		OnMapLongClickListener, OnMarkerClickListener, OnMarkerDragListener,GuidedDialogListener {

	private static final int ZOOM_LEVEL = 20;
	
	private Polyline flightPath;
	private MapPath droneLeashPath;
	private int maxFlightPathSize;
	public boolean isAutoPanEnabled;	
	private boolean guidedModeOnLongPress;

	public boolean hasBeenZoomed = false;

	public GraphicDrone droneMarker;
	public GraphicGuided guided;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);

		droneMarker = new GraphicDrone(this);
		droneLeashPath = new MapPath(mMap, getResources());
		guided = new GraphicGuided(drone);

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
				"pref_max_flight_path_size", "0"));
		isAutoPanEnabled = prefs.getBoolean("pref_auto_pan_enabled", false);
		guidedModeOnLongPress = prefs.getBoolean("pref_guided_mode_on_long_press", true);		
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
		getPreferences();
		if (drone.MavClient.isConnected()) {
			if (drone.guidedPoint.isInitialized()) {
				drone.guidedPoint.newGuidedCoord(DroneHelper.LatLngToCoord(coord));
			} else {
				if (guidedModeOnLongPress) {
					GuidedDialog dialog = new GuidedDialog();
					dialog.setCoord(coord);
					dialog.setListener(this);
					dialog.show(getChildFragmentManager(), "GUIDED dialog");
				}
			}
		}
	}

	@Override
	public void onForcedGuidedPoint(LatLng coord) {
		drone.guidedPoint.forcedGuidedCoordinate(DroneHelper.LatLngToCoord(coord));		
	}

	@Override
	public void onMarkerDragStart(Marker marker) {
	}

	@Override
	public void onMarkerDrag(Marker marker) {
	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		drone.guidedPoint.newGuidedCoord(DroneHelper.LatLngToCoord(marker.getPosition()));
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		drone.guidedPoint.newGuidedCoord(DroneHelper.LatLngToCoord(marker.getPosition()));
		return true;
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		LatLng position = DroneHelper.CoordToLatLang(drone.GPS.getPosition());
		switch (event) {
		case GPS:
			droneLeashPath.update(guided);
			addFlightPathPoint(position);
			animateCamera(position);
			break;
		case GUIDEDPOINT:			
			markers.updateMarker(guided, true, context);
			droneLeashPath.update(guided);
			break;
		default:
			break;
		}
		super.onDroneEvent(event,drone);
	}

	@Override
	protected boolean isMissionDraggable() {
		return false;
	}
}
