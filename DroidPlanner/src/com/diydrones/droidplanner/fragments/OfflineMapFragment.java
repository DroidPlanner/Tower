package com.diydrones.droidplanner.fragments;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.diydrones.droidplanner.helpers.LocalMapTileProvider;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.TileOverlayOptions;

public class OfflineMapFragment extends MapFragment {

	public void setupMap() {
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
	
	/**
	 * Zoom to the extent of the waypoints should be used when the maps has not
	 * undergone the layout phase Assumes a map size of 480x360 px
	 * @param gcpList 
	 */
	public void zoomToExtentsFixed(List<LatLng> pointsList) {
		if (!pointsList.isEmpty()) {
			LatLngBounds.Builder builder = new LatLngBounds.Builder();
			for (LatLng point : pointsList) {
				builder.include(point);
			}
			getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(
					builder.build(), 480, 360, 30));
		}
	}
	
	public void zoomToExtents(List<LatLng> pointsList) {
		if (!pointsList.isEmpty()) {
			LatLngBounds.Builder builder = new LatLngBounds.Builder();
			for (LatLng point : pointsList) {
				builder.include(point);
			}
			getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(
					builder.build(), 30));
		}
	}



}
