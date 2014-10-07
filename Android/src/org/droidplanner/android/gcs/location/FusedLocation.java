package org.droidplanner.android.gcs.location;

import org.droidplanner.core.gcs.location.Location.LocationFinder;
import org.droidplanner.core.gcs.location.Location.LocationReceiver;
import org.droidplanner.core.helpers.coordinates.Coord2D;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

/**
 * Feeds Location Data from Android's FusedLocation LocationProvider
 * 
 */
public class FusedLocation implements LocationFinder, GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		com.google.android.gms.location.LocationListener {

	private static final long MIN_TIME_MS = 500;
	private static final float MIN_DISTANCE_M = 0.0f;
	private LocationClient mLocationClient;
	private LocationReceiver receiver;

	private Location mLastLocation;

	public FusedLocation(Context context) {
		mLocationClient = new LocationClient(context, this, this);
		mLocationClient.connect();
	}

	@Override
	public void enableLocationUpdates() {
		LocationRequest mLocationRequest = LocationRequest.create();
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		mLocationRequest.setInterval(MIN_TIME_MS);
		mLocationRequest.setFastestInterval(MIN_TIME_MS);
		mLocationRequest.setSmallestDisplacement(MIN_DISTANCE_M);
		mLocationClient.requestLocationUpdates(mLocationRequest, this);
	}

	@Override
	public void disableLocationUpdates() {
		if (mLocationClient.isConnected()) {
			mLocationClient.removeLocationUpdates(this);
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {

	}

	@Override
	public void onConnected(Bundle arg0) {

	}

	@Override
	public void onDisconnected() {

	}

	@Override
	public void onLocationChanged(Location androidLocation) {
		if (receiver != null) {
			double distanceToLast = -1.0;
			long timeSinceLast = -1L;

			if(mLastLocation != null) {
				distanceToLast = androidLocation.distanceTo(mLastLocation);
				timeSinceLast = (androidLocation.getTime() - mLastLocation.getTime());
			}

			org.droidplanner.core.gcs.location.Location location = new org.droidplanner.core.gcs.location.Location(
					new Coord2D(androidLocation.getLatitude(), androidLocation.getLongitude()),
					androidLocation.getAccuracy(), androidLocation.getBearing(),
					androidLocation.getSpeed(), distanceToLast, timeSinceLast);

			mLastLocation = androidLocation;
			receiver.onLocationChanged(location);
		}
	}

	@Override
	public void setLocationListner(LocationReceiver receiver) {
		this.receiver = receiver;

	}
}
