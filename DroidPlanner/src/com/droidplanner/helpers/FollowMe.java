package com.droidplanner.helpers;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

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

}
