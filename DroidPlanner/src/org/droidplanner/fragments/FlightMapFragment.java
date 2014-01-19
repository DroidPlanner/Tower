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
	
	private boolean warnOnGuidedMode;

	public boolean hasBeenZoomed = false;

	public DroneMarker droneMarker;
	
	Activity parentActivity;

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
		warnOnGuidedMode = prefs.getBoolean("pref_warn_guided_mode", true);
		
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
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		//we need true current and directly linked Activity object for AlertDialog - kind of bug - we can not use context here.
		parentActivity = activity;
	}	

	@Override
	public void onMapLongClick(LatLng coord) {
		///
		
		//we need new and final variable to use it inside the listener below		
		final LatLng tmpCoord = coord;

		
		OnClickListener positiveButtonClickListener = new AlertDialog.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				drone.guidedPoint.changeGuidedCoordinate(tmpCoord);
			}
		};

		OnClickListener negativeButtonClickListener = new AlertDialog.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
		};		
		
		getPreferences();
		if (warnOnGuidedMode){
			
			final AlertDialog.Builder dlg = new AlertDialog.Builder(parentActivity);
			dlg.setTitle("Guided Mode Warning !!!");
			dlg.setMessage("You are about to enter the Guided Mode");
			dlg.setCancelable(false);
			dlg.setPositiveButton("Ok - go Guided", positiveButtonClickListener);
			dlg.setNegativeButton("Take me back ...", negativeButtonClickListener);
			dlg.create();
			dlg.show();									
		}else{
			//here var is not so important but use the same for clarity.
			drone.guidedPoint.changeGuidedCoordinate(tmpCoord);
		}
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
		super.onDroneEvent(event,drone);
	}
}
