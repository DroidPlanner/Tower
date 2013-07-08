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
import android.util.Log;
import android.widget.Toast;

import com.MAVLink.waypoint;
import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.droidplanner.MAVLink.Drone;
import com.droidplanner.helpers.file.FileStream;
import com.droidplanner.service.MAVLinkClient;

public class FollowMe implements LocationListener {
	private static final long MIN_TIME_MS = 2000;
	private static final float MIN_DISTANCE_M = 0;
	private MAVLinkClient MAV;
	private Context context;
	private boolean followMeEnabled = false;
	private LocationManager locationManager;
	private Drone drone;
	private FileOutputStream file;
	private int count;
	private SharedPreferences prefs;
	
	public FollowMe(MAVLinkClient MAVClient,Context context, Drone drone) {
		this.MAV = MAVClient;
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
		if(isRecordEnabledInPreferences()){
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
		if(isRecordEnabledInPreferences()){
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
		Log.d("GPS", "Location:"+location.getProvider()+" lat "+location.getLatitude()+" :lng "+location.getLongitude()+" :alt "+location.getAltitude()+" :acu "+location.getAccuracy());
		waypoint guidedWP = new waypoint(location.getLatitude(), location.getLongitude(), drone.defaultAlt);	// TODO find a better way to do the altitude
		setGuidedMode(guidedWP);
		if(isRecordEnabledInPreferences()){
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

	
	private void setGuidedMode(waypoint wp) {
		msg_mission_item msg = new msg_mission_item();
		msg.seq = 0;
		msg.current = 2;	//TODO use guided mode enum
		msg.frame = 0; // TODO use correct parameter
		msg.command = 16; // TODO use correct parameter
		msg.param1 = 0; // TODO use correct parameter
		msg.param2 = 0; // TODO use correct parameter
		msg.param3 = 0; // TODO use correct parameter
		msg.param4 = 0; // TODO use correct parameter
		msg.x = (float) wp.coord.latitude;
		msg.y = (float) wp.coord.longitude;
		msg.z = wp.Height.floatValue();
		msg.autocontinue = 1; // TODO use correct parameter
		msg.target_system = 1;
		msg.target_component = 1;
		MAV.sendMavPacket(msg.pack());
	}
	private boolean isEnabledInPreferences() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		
		return prefs.getBoolean("pref_follow_me_mode_enabled", false);	
	}
	
	private boolean isRecordEnabledInPreferences(){
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean("pref_record_me_mode_enabled", false);
		
	}
}
