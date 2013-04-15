package com.droidplanner.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.MAVLink.Drone;
import com.MAVLink.waypoint;
import com.MAVLink.Drone.MapUpdatedListner;
import com.MAVLink.Messages.enums.MAV_TYPE;
import com.droidplanner.R;
import com.droidplanner.SuperActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class FlightMapFragment extends OfflineMapFragment implements OnMapLongClickListener,MapUpdatedListner {
	private static final int DRONE_MIN_ROTATION = 5;
	private GoogleMap mMap;
	private Marker[] DroneMarker;
	private Marker guidedMarker;
	private Polyline flightPath;
	private Polyline missionPath;

	private int maxFlightPathSize;
	private boolean isAutoPanEnabled;
	private boolean isGuidedModeEnabled;
	
	private boolean hasBeenZoomed = false;
	private int lastMarker = 0;
	private OnFlighDataListener mListener;
	private Marker homeMarker;
	private Drone drone;
	
	
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
		
		addDroneMarkersToMap(drone.type);		
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
		isGuidedModeEnabled =prefs.getBoolean("pref_guided_mode_enabled", false);	 // TODO fix preference description in preference.xml
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
	
	private void addFlithPathPoint(LatLng position) {
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

	private void updateDronePosition(double yaw, LatLng coord) {
		double correctHeading = (yaw - getMapRotation()+360)%360;	// This ensure the 0 to 360 range
		int index = (int) (correctHeading/DRONE_MIN_ROTATION);
		
		try{
			DroneMarker[lastMarker].setVisible(false);
			DroneMarker[index].setPosition(coord);
			DroneMarker[index].setVisible(true);
			lastMarker = index;
			
			if(!hasBeenZoomed){
				hasBeenZoomed = true;
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coord, 16));
			}
			
			if(isAutoPanEnabled){
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(DroneMarker[lastMarker].getPosition(), 17));
			}
		}catch(Exception e){
		}
	}
	
	public void zoomToLastKnowPosition() {
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(DroneMarker[lastMarker].getPosition(), 16));
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
	
	public void updateDroneMarkers(){
		for (Marker marker : DroneMarker) {
			marker.remove();
		}
		addDroneMarkersToMap(drone.type);
	}
	
	private void addDroneMarkersToMap(int type) {
		int count = 360/DRONE_MIN_ROTATION;
		
		DroneMarker = new Marker[count];
		for (int i = 0; i < count; i++) {					
			DroneMarker[i] = mMap.addMarker(new MarkerOptions()
					.anchor((float) 0.5, (float) 0.5)
					.position(new LatLng(0, 0))
					.icon(generateDroneIcon(i*DRONE_MIN_ROTATION,type))
					.visible(false));
		}
			
	}
	
	private BitmapDescriptor generateDroneIcon(float heading,int type) {
		Bitmap planeBitmap = getDroneBitmap(type);
		Matrix matrix = new Matrix();
		matrix.postRotate(heading - mMap.getCameraPosition().bearing);
		return BitmapDescriptorFactory.fromBitmap( Bitmap.createBitmap(planeBitmap, 0, 0, planeBitmap.getWidth(),
				planeBitmap.getHeight(), matrix, true));
	}

	@Override
	public void onMapLongClick(LatLng point) {
		if (isGuidedModeEnabled) {
			mListener.onSetGuidedMode(point);	
			updateGuidedMarker(point);
		}
	}

	@Override
	public void onDroneUpdate() {
		updateDronePosition(drone.yaw, drone.position);
		addFlithPathPoint(drone.position);
		
	}

	
	private Bitmap getDroneBitmap(int type) {
		switch (type) {
		case MAV_TYPE.MAV_TYPE_TRICOPTER:
		case MAV_TYPE.MAV_TYPE_QUADROTOR:
		case MAV_TYPE.MAV_TYPE_HEXAROTOR:
		case MAV_TYPE.MAV_TYPE_OCTOROTOR:
		case MAV_TYPE.MAV_TYPE_HELICOPTER:
			return BitmapFactory
					.decodeResource(getResources(), R.drawable.quad);
		case MAV_TYPE.MAV_TYPE_FIXED_WING:
		default:
			return BitmapFactory.decodeResource(getResources(),
					R.drawable.plane);
		}
	}

}
