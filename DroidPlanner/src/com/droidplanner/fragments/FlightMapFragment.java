package com.droidplanner.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.MAVLink.waypoint;
import com.droidplanner.R;
import com.droidplanner.MAVLink.Drone;
import com.droidplanner.MAVLink.Drone.MapUpdatedListner;
import com.droidplanner.activitys.SuperActivity;
import com.droidplanner.fragments.markers.DroneMarker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class FlightMapFragment extends OfflineMapFragment implements OnMapLongClickListener,MapUpdatedListner {
	public GoogleMap mMap;
	private Marker guidedMarker;
	private Polyline flightPath;
	private Polyline missionPath;

	private int maxFlightPathSize;
	public boolean isAutoPanEnabled;
	private boolean isGuidedModeEnabled;
	
	public boolean hasBeenZoomed = false;
	private OnFlighDataListener mListener;
	private Marker homeMarker;
	public DroneMarker droneMarker = new DroneMarker();
	public Drone drone;
	
	
	public interface OnFlighDataListener {
		public void onSetGuidedMode(LatLng point);
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);
		mMap = getMap();		
		drone = ((SuperActivity)getActivity()).app.drone;
		drone.setMapListner(this);		
		
		droneMarker.buildBitmaps(this, drone.getType());		
		addFlightPathToMap();	
		addMissionPathToMap();
		getPreferences();
		mMap.setOnMapLongClickListener(this);
		
		
		return view;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mListener = (OnFlighDataListener) activity;
	}
	
	private void getPreferences() {
		Context context = this.getActivity();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		maxFlightPathSize =Integer.valueOf(prefs.getString("pref_max_fligth_path_size", "0"));
		isGuidedModeEnabled =prefs.getBoolean("pref_guided_mode_enabled", false);
		isAutoPanEnabled =prefs.getBoolean("pref_auto_pan_enabled", false);	
	}


	public void updateMissionPath(Drone drone) {
		ArrayList<LatLng> missionPoints = new ArrayList<LatLng>();
		missionPoints.add(drone.getHome().coord);
		for (waypoint point : drone.getWaypoints()) {
			missionPoints.add(point.coord);
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

	private void updateGuidedMarker(LatLng point) {
		if(guidedMarker == null){
			guidedMarker = mMap.addMarker(new MarkerOptions()
			.anchor((float) 0.5, (float) 0.5)
			.position(point)
			.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));	
		} else {
			guidedMarker.setPosition(point);
		}	
	}

	public void zoomToLastKnowPosition() {
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(droneMarker.droneMarker.getPosition(), 16));
	}

	private void addMissionPathToMap() {
		PolylineOptions missionPathOptions = new PolylineOptions();
		missionPathOptions.color(Color.YELLOW).width(3).zIndex(0);
		missionPath = mMap.addPolyline(missionPathOptions);
	}
	
	public void updateHomeToMap(Drone drone) {
		if(homeMarker== null){
		homeMarker = mMap.addMarker(new MarkerOptions()
		.position(drone.getHome().coord)
		.snippet(
				String.format(Locale.ENGLISH, "%.2f",
						drone.getHome().Height))
		.draggable(true)
		.anchor((float) 0.5, (float) 0.5)
		.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_menu_home))
		.title("Home"));
		}else {
			homeMarker.setPosition(drone.getHome().coord);
			homeMarker.setSnippet(
				String.format(Locale.ENGLISH, "%.2f",
						drone.getHome().Height));
		}
	}
	
	private void addFlightPathToMap() {
		PolylineOptions flightPathOptions = new PolylineOptions();
		flightPathOptions.color(Color.argb(180, 0, 0, 200)).width(2).zIndex(1);
		flightPath = mMap.addPolyline(flightPathOptions);		
	}
	
	@Override
	public void onMapLongClick(LatLng point) {
		getPreferences();
		if (isGuidedModeEnabled) {
			mListener.onSetGuidedMode(point);	
			updateGuidedMarker(point);
		}
	}

	/**
	 * @deprecated Use {@link com.droidplanner.fragments.markers.DroneMarker#onDroneUpdate(com.droidplanner.fragments.FlightMapFragment)} instead
	 */
	@Override
	public void onDroneUpdate() {
		droneMarker.onDroneUpdate(this);
	}

}
