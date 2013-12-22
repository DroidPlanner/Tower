package com.droidplanner.helpers;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.DroneEventsType;
import com.google.android.gms.maps.model.LatLng;

public class RecordMe implements LocationListener {
	private static final long MIN_TIME_MS = 2000;
	private static final float MIN_DISTANCE_M = 0;

	private Context context;
	private Drone drone;
	private LocationManager locationManager;
	private boolean recordMeEnabled = false;

	public RecordMe(Context context, Drone drone) {
		this.context = context;
		this.drone = drone;
		this.locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
	}

	public void toogleRecordMeState() {
		if (isEnabled()) {
			finishRecordMe();
		} else {
			startRecordMe();
		}
	}

	private void startRecordMe() {
		Toast.makeText(context, "Record Enabled", Toast.LENGTH_SHORT).show();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				MIN_TIME_MS, MIN_DISTANCE_M, this);
		recordMeEnabled = true;
	}

	private void finishRecordMe() {
		Toast.makeText(context, "Record Disabled", Toast.LENGTH_SHORT).show();
		locationManager.removeUpdates(this);
		recordMeEnabled = false;
	}

	public boolean isEnabled() {
		return recordMeEnabled;
	}

	// @Override
	public void onLocationChanged(Location location) {
		// TODO find a better way to do the altitude
		LatLng coord = new LatLng(location.getLatitude(), location.getLongitude());
		drone.mission.addWaypoint(coord, drone.mission.getDefaultAlt());
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}
