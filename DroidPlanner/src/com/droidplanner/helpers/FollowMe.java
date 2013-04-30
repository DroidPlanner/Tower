package com.droidplanner.helpers;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.MAVLink.waypoint;
import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.droidplanner.service.MAVLinkClient;

public class FollowMe implements LocationListener {
	private MAVLinkClient MAV;
	private Context context;
	private boolean followMeEnabled = false;
	private LocationManager locationManager;
	
	public FollowMe(MAVLinkClient MAVClient,Context context) {
		this.MAV = MAVClient;
		this.context = context;
		this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	}
	public void toogleFollowMeState() {
		if(isEnabled()){
			disableFollowMe();				
		}else {
			enableFollowMe();
		}
	}
	
	private void enableFollowMe() {
		//TODO add the function that handles the follow me function
		Toast.makeText(context, "FollowMe Enabled", Toast.LENGTH_SHORT).show();
				
		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		
		followMeEnabled = true;		
	}
	
	private void disableFollowMe() {
		//TODO add the disable for the follow me mode
		Toast.makeText(context, "FollowMe Disabled", Toast.LENGTH_SHORT).show();
		followMeEnabled = false;
	}
	
	public boolean isEnabled() {
		return followMeEnabled;
	}

	
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		Log.d("GPS", "Location:"+location.getProvider()+" lat "+location.getLatitude()+" :lng "+location.getLongitude()+" :alt "+location.getAltitude()+" :acu "+location.getAccuracy());
		
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

	
	public void setGuidedMode(waypoint wp) {
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
}
