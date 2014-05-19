package org.droidplanner.android.gcs;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.geoTools.GeoTools;

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

public class FollowMe implements GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		com.google.android.gms.location.LocationListener, OnDroneListener {
	private static final long MIN_TIME_MS = 500;
	private static final float MIN_DISTANCE_M = 1;
	private static final double LEASH_LENGTH = 10.0;
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
		// drone.state.setMode(ApmModes.ROTOR_GUIDED);
	}

	private void disableFollowMe() {
		if(followMeEnabled){
			Toast.makeText(context, "FollowMe Disabled", Toast.LENGTH_SHORT).show();
			followMeEnabled = false;
			Log.d("follow", "disable");
		}
		mLocationClient.removeLocationUpdates(this);
	}

	public boolean isEnabled() {
		return followMeEnabled;
	}

	private boolean isEnabledInPreferences() {
		return true; // TODO remove this method if not needed
		/*
		 * SharedPreferences prefs = PreferenceManager
		 * .getDefaultSharedPreferences(context);
		 * 
		 * return prefs.getBoolean("pref_follow_me_mode_enabled", false);
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
		switch (event) {
		case MODE:
			if ((drone.state.getMode() != ApmModes.ROTOR_GUIDED)) {
				disableFollowMe();
			}
			break;
		default:
			return;

		}

	}

	@Override
	public void onLocationChanged(Location location) {
		Coord2D gcsCoord = new Coord2D(location.getLatitude(),
				location.getLongitude());
		float bearing = location.getBearing();
		Log.d("follow", gcsCoord.toString());

		// TODO implement some sort of Follow-me type selection
		//processNewLocationAsOverYourHead(gcsCoord,bearing);
		//processNewLocationAsLeash(gcsCoord,bearing);
		//processNewLocationAsFixedAngle(gcsCoord,bearing);
		processNewLocationAsHeadingAngle(gcsCoord,bearing);
		
	}

	private void processNewLocationAsHeadingAngle(Coord2D gcsCoord, float bearing) {
		Coord2D goCoord = GeoTools.newCoordFromBearingAndDistance(gcsCoord,
				bearing+90.0, LEASH_LENGTH);
		drone.guidedPoint.newGuidedCoord(goCoord);	
	}

	private void processNewLocationAsFixedAngle(Coord2D gcsCoord, float bearing) {
			Coord2D goCoord = GeoTools.newCoordFromBearingAndDistance(gcsCoord,
					90.0, LEASH_LENGTH);
			drone.guidedPoint.newGuidedCoord(goCoord);
	}

	private void processNewLocationAsOverYourHead(Coord2D gcsCoord, float bearing) {
		drone.guidedPoint.newGuidedCoord(gcsCoord);
	}

	private void processNewLocationAsLeash(Coord2D gcsCoord, float bearing) {
		if (GeoTools.getDistance(gcsCoord, drone.GPS.getPosition())
				.valueInMeters() > LEASH_LENGTH) {
			double headingGCStoDrone = GeoTools.getHeadingFromCoordinates(
					gcsCoord, drone.GPS.getPosition());
			Coord2D goCoord = GeoTools.newCoordFromBearingAndDistance(gcsCoord,
					headingGCStoDrone, LEASH_LENGTH);
			drone.guidedPoint.newGuidedCoord(goCoord);
		}
	}

}