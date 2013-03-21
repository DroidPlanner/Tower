package com.droidplanner.fragments;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.droidplanner.helpers.LocalMapTileProvider;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.TileOverlayOptions;

public class OfflineMapFragment extends MapFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);
		setupMap();
		return view;
	}

	private void setupMap() {
		setupMapUI();
		setupMapOverlay();
	}

	private void setupMapUI() {
		GoogleMap mMap = getMap();
		mMap.setMyLocationEnabled(true);
		UiSettings mUiSettings = mMap.getUiSettings();
		mUiSettings.setMyLocationButtonEnabled(true);
		mUiSettings.setCompassEnabled(true);
		mUiSettings.setTiltGesturesEnabled(false);
	}

	private void setupMapOverlay() {
		if (isOfflineMapEnabled()) {
			setupOfflineMapOverlay();
		} else {
			setupOnlineMapOverlay();
		}
	}

	private boolean isOfflineMapEnabled() {
		Context context = this.getActivity();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		return prefs.getBoolean("pref_advanced_use_offline_maps", false);
	}

	private void setupOnlineMapOverlay() {
		GoogleMap mMap = getMap();
		mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
	}

	private void setupOfflineMapOverlay() {
		GoogleMap mMap = getMap();
		mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
		mMap.addTileOverlay(new TileOverlayOptions()
				.tileProvider(new LocalMapTileProvider()));
	}

	public void zoomToExtents(List<LatLng> pointsList) {
		if (!pointsList.isEmpty()) {
			LatLngBounds bounds = getBounds(pointsList);
			CameraUpdate animation;
			if (isMapLayoutFinished())
				animation = CameraUpdateFactory.newLatLngBounds(bounds, 30);
			else
				animation = CameraUpdateFactory.newLatLngBounds(bounds, 480,
						360, 30);
			getMap().animateCamera(animation);
		}
	}

	private boolean isMapLayoutFinished() {
		return getMap() != null;
	}

	private LatLngBounds getBounds(List<LatLng> pointsList) {
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		for (LatLng point : pointsList) {
			builder.include(point);
		}
		return builder.build();
	}

}
