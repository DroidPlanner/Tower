package com.droidplanner.helpers;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.MAVLink.waypoint;
import com.droidplanner.R;
import com.droidplanner.MAVLink.Drone;
import com.droidplanner.service.MAVLinkClient;
import com.droidplanner.waypoints.MissionWriter;

public class RecordMe implements LocationListener {
	private static final long MIN_TIME_MS = 2000;
	private static final float MIN_DISTANCE_M = 0;

	private Context context;
	private Drone drone;
	private LocationManager locationManager;
	private boolean recordMeEnabled = false;
	private List<waypoint> waypoints = new ArrayList<waypoint>();

	public RecordMe(MAVLinkClient MAVClient, Context context, Drone drone) {
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

		// Register the listener with the Location Manager to receive location
		// updates
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				MIN_TIME_MS, MIN_DISTANCE_M, this);
		recordMeEnabled = true;
	}

	private void finishRecordMe() {
		locationManager.removeUpdates(this);
		recordMeEnabled = false;
		saveWaypointsToFile();
		waypoints.clear();
	}

	private void saveWaypointsToFile() {
		if (writeMission()) {
			Toast.makeText(context, R.string.file_saved, Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(context, R.string.error_when_saving,
					Toast.LENGTH_SHORT).show();
		}
	}

	private boolean writeMission() {
		if (waypoints.size() > 1) {
			waypoint home = waypoints.get(0);
			waypoints.remove(0);
			MissionWriter missionWriter = new MissionWriter(home, waypoints,
					"RecordMe");
			return missionWriter.saveWaypoints();
		} else {
			return false;
		}
	}

	public boolean isEnabled() {
		return recordMeEnabled;
	}

	// @Override
	public void onLocationChanged(Location location) {
		// TODO find a better way to do the altitude
		waypoints.add(new waypoint(location.getLatitude(), location
				.getLongitude(), drone.defaultAlt));
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
