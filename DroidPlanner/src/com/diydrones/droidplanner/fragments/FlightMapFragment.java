package com.diydrones.droidplanner.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.diydrones.droidplanner.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class FlightMapFragment extends MapFragment {
	private Bitmap planeBitmap;
	private GoogleMap mMap;

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

		return view;
	}

	public void updateDronePosition(float heading, LatLng coord) {
		mMap.clear(); // TODO Find a better implementation, where all markers
						// don't need to be cleared
		addDroneMarkerToMap(heading, coord);
	}

	private void addDroneMarkerToMap(float heading, LatLng coord) {
			// TODO Find a way to rotate the plane that doesn't consume too much CPU power
			Matrix matrix = new Matrix();
			matrix.postRotate(heading - mMap.getCameraPosition().bearing);
			Bitmap rotatedPlane = Bitmap.createBitmap(planeBitmap, 0, 0,
					planeBitmap.getWidth(), planeBitmap.getHeight(), matrix,
					true);
			mMap.addMarker(new MarkerOptions().position(coord)
					.anchor((float) 0.5, (float) 0.5)
					.icon(BitmapDescriptorFactory.fromBitmap(rotatedPlane)));
			
	}

}
