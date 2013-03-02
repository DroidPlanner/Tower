package com.diydrones.droidplanner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.MAVLink.GPSMananger;
import com.MAVLink.Messages.MAVLinkMessage;
import com.diydrones.droidplanner.service.MAVLinkClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class FightDataActivity extends Activity {

	private GoogleMap mMap;
	private MenuItem connectButton;
	private Bitmap planeBitmap;

	@Override
	int getNavigationItem() {
		return 2;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		planeBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.planetracker);
		setContentView(R.layout.flightdata);
	
		setUpMapIfNeeded();
	
		MAVClient.init();
	}

	@Override
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		MAVClient.onDestroy();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_flightdata, menu);
		connectButton = menu.findItem(R.id.menu_connect);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.menu_connect:
			MAVClient.sendConnectMessage();
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			// Try to obtain the map from the SupportMapFragment.
			mMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				setUpMap();
			}
		}
	}

	private void setUpMap() {
		mMap.setMyLocationEnabled(true);
		mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
	
		UiSettings mUiSettings = mMap.getUiSettings();
		mUiSettings.setMyLocationButtonEnabled(true);
		mUiSettings.setCompassEnabled(true);
		mUiSettings.setTiltGesturesEnabled(false);
	
	}

	public MAVLinkClient MAVClient = new MAVLinkClient(this) {
		@Override
		public void notifyReceivedData(MAVLinkMessage msg) {
			gpsManager.processMessage(msg);
		}

		@Override
		public void notifyDisconnected() {
			connectButton.setTitle(getResources().getString(
					R.string.menu_connect));
		}

		@Override
		public void notifyConnected() {
			connectButton.setTitle(getResources().getString(
					R.string.menu_disconnect));
		}
	};

	GPSMananger gpsManager = new GPSMananger(MAVClient) {
		@Override
		public void onGpsDataReceived(GPSdata data) {
			// Log.d("GPS",
			// "LAT:"+data.position.coord.latitude+" LNG:"+data.position.coord.longitude+"ALT:"+data.position.Height+" heading:"+data.heading);
			mMap.clear(); // Find a better implementation, where all markers
							// don't need to be cleared
			addDroneMarkerToMap(data.heading, data.position.coord);

		}
	};

	/**
	 * @param data
	 */
	private void addDroneMarkerToMap(float heading, LatLng coord) {
		Matrix matrix = new Matrix();
		matrix.postRotate(heading - mMap.getCameraPosition().bearing);
		Bitmap rotatedPlane = Bitmap.createBitmap(planeBitmap, 0, 0,
				planeBitmap.getWidth(), planeBitmap.getHeight(), matrix, true);
		mMap.addMarker(new MarkerOptions().position(coord)
				.anchor((float) 0.5, (float) 0.5)
				.icon(BitmapDescriptorFactory.fromBitmap(rotatedPlane)));
	}

}
