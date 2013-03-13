package com.diydrones.droidplanner.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_global_position_int;
import com.diydrones.droidplanner.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class FlightMapFragment extends MapFragment {
	private Bitmap planeBitmap;
	private GoogleMap mMap;
	private Marker DroneMarker;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);
		planeBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.planetracker);

		mMap = getMap();
		mMap.setMyLocationEnabled(true);
		mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

		UiSettings mUiSettings = mMap.getUiSettings();
		mUiSettings.setMyLocationButtonEnabled(true);
		mUiSettings.setCompassEnabled(true);
		mUiSettings.setTiltGesturesEnabled(false);
		
		addDroneMarkerToMap();

		return view;
	}

	public void updateDronePosition(float heading, LatLng coord) {
		DroneMarker.setPosition(coord); // TODO This causes the heap to grow a lot.
		if(!DroneMarker.isVisible()){
			DroneMarker.setVisible(true);
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coord, 16));
		}
	}

	private void addDroneMarkerToMap() {
			// TODO Find a way to rotate the plane that doesn't consume too much CPU power
			/*Matrix matrix = new Matrix();
			matrix.postRotate(heading - mMap.getCameraPosition().bearing);
			Bitmap rotatedPlane = Bitmap.createBitmap(planeBitmap, 0, 0,
					planeBitmap.getWidth(), planeBitmap.getHeight(), matrix,
					true);
					
			mMap.addMarker(new MarkerOptions().position(coord)
					.anchor((float) 0.5, (float) 0.5)
					.icon(BitmapDescriptorFactory.fromBitmap(rotatedPlane)));
			*/
			DroneMarker = mMap.addMarker(new MarkerOptions()
					.anchor((float) 0.5, (float) 0.5)
					.position(new LatLng(0, 0))
					.icon(BitmapDescriptorFactory.fromBitmap(planeBitmap))
					.visible(false));
			
	}

	public void receiveData(MAVLinkMessage msg) {
		if(msg.msgid == msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT) {
			LatLng position = new LatLng(((msg_global_position_int)msg).lat/1E7, ((msg_global_position_int)msg).lon/1E7);
			float heading = (0x0000FFFF & ((int)((msg_global_position_int)msg).hdg))/100f; // TODO fix unsigned short read at mavlink library
			updateDronePosition(heading, position);
		}
	}

}
