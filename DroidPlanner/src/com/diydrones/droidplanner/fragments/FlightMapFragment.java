package com.diydrones.droidplanner.fragments;

import java.util.List;

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

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_global_position_int;
import com.diydrones.droidplanner.R;
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

public class FlightMapFragment extends OfflineMapFragment implements OnMapLongClickListener {
	private static final int DRONE_MIN_ROTATION = 5;
	private GoogleMap mMap;
	private Marker[] DroneMarker;
	private Marker guidedMarker;
	private Polyline flightPath;

	private int maxFlightPathSize;
	private boolean isAutoPanEnabled;
	private boolean isGuidedModeEnabled;
	
	private boolean hasBeenZoomed = false;
	private int lastMarker = 0;
	private OnFlighDataListener mListener;
	
	
	public interface OnFlighDataListener {
		public void onSetGuidedMode(LatLng point);
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);
		mMap = getMap();		
		addDroneMarkersToMap();		
		addFlightPathToMap();	
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

	public void receiveData(MAVLinkMessage msg) {
		if(msg.msgid == msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT) {
			LatLng position = new LatLng(((msg_global_position_int)msg).lat/1E7, ((msg_global_position_int)msg).lon/1E7);
			float heading = (0x0000FFFF & ((int)((msg_global_position_int)msg).hdg))/100f; // TODO fix unsigned short read at mavlink library
			updateDronePosition(heading, position);
			addFlithPathPoint(position);
			
		}
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

	private void updateDronePosition(float heading, LatLng coord) {
		float correctHeading = (heading - mMap.getCameraPosition().bearing+360)%360;	// This ensure the 0 to 360 range
		int index = (int) (correctHeading/DRONE_MIN_ROTATION);
		
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
	}
	
	public void zoomToLastKnowPosition() {
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(DroneMarker[lastMarker].getPosition(), 16));
	}

	private void addFlightPathToMap() {
		PolylineOptions flightPathOptions = new PolylineOptions();
		flightPathOptions.color(Color.argb(128, 0, 0, 200)).width(2);
		flightPath = mMap.addPolyline(flightPathOptions);		
	}
	
	private void addDroneMarkersToMap() {
		int count = 360/DRONE_MIN_ROTATION;
		
		DroneMarker = new Marker[count];
		for (int i = 0; i < count; i++) {					
			DroneMarker[i] = mMap.addMarker(new MarkerOptions()
					.anchor((float) 0.5, (float) 0.5)
					.position(new LatLng(0, 0))
					.icon(generateDroneIcon(i*DRONE_MIN_ROTATION))
					.visible(false));
		}
			
	}
	
	private BitmapDescriptor generateDroneIcon(float heading) {
		Bitmap planeBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.planetracker);
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


}
