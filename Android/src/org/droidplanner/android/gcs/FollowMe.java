package org.droidplanner.android.gcs;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.helpers.coordinates.Coord2D;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.MAVLink.Messages.ApmModes;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

public class FollowMe implements GooglePlayServicesClient.ConnectionCallbacks,GooglePlayServicesClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, OnDroneListener {
	private static final long MIN_TIME_MS = 200;
	private static final float MIN_DISTANCE_M = 0;
	private Context context;
	private boolean followMeEnabled = false;
	private Drone drone;
	private LocationClient mLocationClient;

	public FollowMe(Context context, Drone drone) {
		this.context = context;
		this.drone = drone;
		mLocationClient = new LocationClient(context, this, this);
		mLocationClient.connect();
		drone.events.addDroneListener(this);
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
		Log.d("follow", "enable");
		Toast.makeText(context, "FollowMe Enabled", Toast.LENGTH_SHORT).show();

		// Register the listener with the Location Manager to receive location
		// updates
		
		LocationRequest mLocationRequest = LocationRequest.create();
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		mLocationRequest.setInterval(MIN_TIME_MS);
		mLocationRequest.setFastestInterval(MIN_TIME_MS);
		mLocationRequest.setSmallestDisplacement(MIN_DISTANCE_M);
		mLocationClient.requestLocationUpdates(mLocationRequest, this);

		followMeEnabled = true;
		drone.state.setMode(ApmModes.ROTOR_GUIDED);
	}

	private void disableFollowMe() {
		Toast.makeText(context, "FollowMe Disabled", Toast.LENGTH_SHORT).show();
		Log.d("follow", "disable");
		mLocationClient.removeLocationUpdates(this);
		followMeEnabled = false;
	}

	public boolean isEnabled() {
		return followMeEnabled;
	}

	@Override
	public void onLocationChanged(Location location) {
		Coord2D coord = new Coord2D(location.getLatitude(),
				location.getLongitude());
		Log.d("follow", coord.toString());
		drone.guidedPoint.newGuidedCoord(coord);
	}

	private boolean isEnabledInPreferences() {
		return true; // TODO remove this method if not needed
		/*
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		return prefs.getBoolean("pref_follow_me_mode_enabled", false);
		*/
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch(event){
		case MODE:
			if ((drone.state.getMode() != ApmModes.ROTOR_GUIDED)) {
				disableFollowMe();
			}			
			break;
		default:
			return;
		
		}
		
	}
}