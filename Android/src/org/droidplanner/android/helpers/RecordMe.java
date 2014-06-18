package org.droidplanner.android.helpers;

import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.core.helpers.coordinates.Coord2D;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

public class RecordMe implements LocationListener {
	private static final long MIN_TIME_MS = 2000;
	private static final float MIN_DISTANCE_M = 0;

	private Context context;
    private final MissionProxy missionProxy;
	private LocationManager locationManager;
	private boolean recordMeEnabled = false;

	public RecordMe(Context context, MissionProxy missionProxy) {
		this.context = context;
        this.missionProxy = missionProxy;
		this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
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
		Coord2D coord = new Coord2D(location.getLatitude(),
				location.getLongitude());
        missionProxy.addWaypoint(coord);
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
