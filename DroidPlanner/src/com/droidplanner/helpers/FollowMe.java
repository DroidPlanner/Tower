package com.droidplanner.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.MAVLink.Messages.ApmModes;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.variables.waypoint;

// Mode change failsafe listener
import com.droidplanner.drone.DroneInterfaces.ModeChangedListener;

public class FollowMe implements LocationListener, ModeChangedListener {
	private static final long MIN_TIME_MS = 2000;
	private static final float MIN_DISTANCE_M = 0;
	private Context context;
	private boolean followMeEnabled = false;
	private LocationManager locationManager;
	private Drone drone;

	public FollowMe(Context context, Drone drone) {
		this.context = context;
		this.drone = drone;
		this.locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		
		// Register ourselves with the modeChangedListener so we get mode updates
		drone.setFollowMeListener(this);
	}

	public void toogleFollowMeState() {
		if (isEnabledInPreferences()) {
			if (isEnabled()) {
				disableFollowMe();				
			} else {
				
				// Vehicle has to be connected for our commands to reach it
				if (drone.MavClient.isConnected()) {
					
					// Vehicle has to be armed for our commands to alter the flight
					if (drone.state.isArmed()) {
												
						// Turn on follow me using existing method
						enableFollowMe();
						
					} else {
						
						// The vehicle is not armed, give an error
						Toast.makeText(context, "Drone Not Armed", Toast.LENGTH_SHORT).show();
					}
				} else {
					
					// MAVLink is not connected, give an error
					Toast.makeText(context, "Drone Not Connected", Toast.LENGTH_SHORT).show();
				}
			}
		} else {
			disableFollowMe();
		}
	}

	private void enableFollowMe() {
		Toast.makeText(context, "FollowMe Enabled", Toast.LENGTH_SHORT).show();
		
		// Register the listener with the Location Manager to receive location
		// updates
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				MIN_TIME_MS, MIN_DISTANCE_M, this);
		
		// Guided mode so our waypoints change the flight 
		drone.state.changeFlightMode(ApmModes.ROTOR_GUIDED);

		followMeEnabled = true;
		
	}

	private void disableFollowMe() {
		Toast.makeText(context, "FollowMe Disabled", Toast.LENGTH_SHORT).show();
		locationManager.removeUpdates(this);
		
		// For safety set the vehicle to Loiter mode specifically
		drone.state.changeFlightMode(ApmModes.ROTOR_LOITER);
		
		followMeEnabled = false;
	}

	public boolean isEnabled() {
		return followMeEnabled;
	}

	@Override
	public void onLocationChanged(Location location) {
		
		// Make a new guided waypoint from our current location as given by GPS, altitude from planning value
		waypoint guidedWP = new waypoint(location.getLatitude(),
				location.getLongitude(), drone.mission.getDefaultAlt());
		// TODO find a better way to do the GUIDED altitude
		drone.guidedPoint.setGuidedMode(guidedWP);
	}

	@Override
	public void onProviderDisabled(String provider) {
		
		// Check if follow me is enabled as the app has lost GPS from the device
		if (isEnabled())
		{
			
			// Since we don't have GPS coming in, turn off follow me
			disableFollowMe();
			
		}
		
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

		return prefs.getBoolean("pref_follow_me_mode_enabled", false);
	}
	
	/* For all modes other than guided turn off follow me as only guided should be used for follow me
	 * 
	 * Without this the tablet will keep sending a new waypoint and setting guided mode using
	 * follow me. This will cause the vehicle to be uncontrollable via RC radio,
	 * failsafe, or ground control station.
	 * 
	 */
	@Override
	public void onModeChanged()
	{

		// Guided mode is correct, don't do anything, for other modes cancel follow me
		if (drone.state.getMode() != ApmModes.ROTOR_GUIDED && followMeEnabled)
		{

			// Vehicle is not in guided mode, disable follow me using existing method
			disableFollowMe();

		}

	}

}