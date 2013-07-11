package com.droidplanner.helpers;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.droidplanner.MAVLink.Drone;
import com.droidplanner.helpers.file.FileStream;
import com.droidplanner.service.MAVLinkClient;

public class RecordMe implements LocationListener {
	private static final long MIN_TIME_MS = 2000;
	private static final float MIN_DISTANCE_M = 0;
	private Context context;
	private boolean followMeEnabled = false;
	private LocationManager locationManager;
	private Drone drone;
	private FileOutputStream file;
	private int count;
	
	public RecordMe(MAVLinkClient MAVClient,Context context, Drone drone) {
		this.context = context;
		this.drone = drone;
		this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	}

	public void toogleFollowMeState() {
		if (isEnabledInPreferences()) {
			if (isEnabled()) {
				disableFollowMe();
			} else {
				enableFollowMe();
			}
		} else {
			disableFollowMe();
		}
	}
	
	private void enableFollowMe() {
		Toast.makeText(context, "FollowMe Enabled", Toast.LENGTH_SHORT).show();
				
		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_MS, MIN_DISTANCE_M, this);
		count=0;
		followMeEnabled = true;
		if(isEnabledInPreferences()){
			try {
				file = FileStream.getWaypointFileStream();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}
	
	private void disableFollowMe() {
		Toast.makeText(context, "FollowMe Disabled", Toast.LENGTH_SHORT).show();
		locationManager.removeUpdates(this);
		followMeEnabled = false;
		if(isEnabledInPreferences()){
			try {
				file.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public boolean isEnabled() {
		return followMeEnabled;
	}
	
	//record me writes location to waypoint file
	public void writeFirstLine(Location l){
		try {
			file.write("QGC WPL 110\n0\t1\t0\t16\t0\t0\t0\t0\t".getBytes());
			file.write(String.valueOf(l.getLatitude()).getBytes());
			file.write("\t".getBytes());
			file.write(String.valueOf(l.getLongitude()).getBytes());
			file.write("\t".getBytes());
			file.write(String.valueOf(drone.defaultAlt).getBytes());
			file.write("\t1\n".getBytes());
		} catch (IOException e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void saveWaypoints(Location l) {
		try {			
			file.write(String.valueOf(count).getBytes());
			file.write("\t0\t0\t16\t0\t0\t0\t0\t".getBytes());
			file.write(String.valueOf(l.getLatitude()).getBytes());
			file.write("\t".getBytes());
			file.write(String.valueOf(l.getLongitude()).getBytes());
			file.write("\t".getBytes());
			file.write(String.valueOf(drone.defaultAlt).getBytes());
			file.write("\t1\n".getBytes());
				
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onLocationChanged(Location location) {
		if(isEnabledInPreferences()){
			if(count==0){
				writeFirstLine(location);
				count++;
			}else{
				saveWaypoints(location);
				count++;
			}
		}
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

	private boolean isEnabledInPreferences() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		return prefs.getBoolean("pref_record_me_mode_enabled", false);
	}
}
