package org.droidplanner.android.gcs.location;

import org.droidplanner.android.utils.GoogleApiClientManager;
import org.droidplanner.android.utils.GoogleApiClientManager.GoogleApiClientTask;
import org.droidplanner.core.gcs.location.Location.LocationFinder;
import org.droidplanner.core.gcs.location.Location.LocationReceiver;
import org.droidplanner.core.helpers.coordinates.Coord2D;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Feeds Location Data from Android's FusedLocation LocationProvider
 * 
 */
public class FusedLocation implements LocationFinder, com.google.android.gms.location.LocationListener {

    private static final String TAG = FusedLocation.class.getSimpleName();

	private static final long MIN_TIME_MS = 1000;
	private static final float MIN_DISTANCE_M = 0.0f;
    private static final float LOCATION_ACCURACY_THRESHOLD = 15.0f;
    private static final float JUMP_FACTOR = 4.0f;

    private final GoogleApiClientManager gApiMgr;
    private final GoogleApiClientTask requestLocationUpdate;
    private final GoogleApiClientTask removeLocationUpdate;

	private LocationReceiver receiver;

	private Location mLastLocation;

    private float mTotalSpeed;
    private long mSpeedReadings;

	public FusedLocation(Context context) {
        gApiMgr = new GoogleApiClientManager(context, LocationServices.API);

        requestLocationUpdate = gApiMgr.new GoogleApiClientTask() {
            @Override
            protected void doRun() {
                final LocationRequest locationRequest = LocationRequest.create();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationRequest.setInterval(MIN_TIME_MS);
                locationRequest.setFastestInterval(MIN_TIME_MS);
                locationRequest.setSmallestDisplacement(MIN_DISTANCE_M);
                LocationServices.FusedLocationApi.requestLocationUpdates(getGoogleApiClient(),
                        locationRequest, FusedLocation.this);
            }
        };

        removeLocationUpdate = gApiMgr.new GoogleApiClientTask() {
            @Override
            protected void doRun() {
                LocationServices.FusedLocationApi.removeLocationUpdates(getGoogleApiClient(),
                        FusedLocation.this);
            }
        };

        gApiMgr.start();
	}

	@Override
	public void enableLocationUpdates() {
        mSpeedReadings = 0;
        mTotalSpeed = 0f;
        gApiMgr.addTask(requestLocationUpdate);
	}

	@Override
	public void disableLocationUpdates() {
        gApiMgr.addTask(removeLocationUpdate);
	}

	@Override
	public void onLocationChanged(Location androidLocation) {
		if (receiver != null) {
			float distanceToLast = -1.0f;
			long timeSinceLast = -1L;

			if(mLastLocation != null) {
				distanceToLast = androidLocation.distanceTo(mLastLocation);
				timeSinceLast = (androidLocation.getTime() - mLastLocation.getTime()) / 1000;
			}

            final float currentSpeed = distanceToLast > 0f && timeSinceLast > 0
                    ? (distanceToLast / timeSinceLast)
                    : 0f;
            final boolean isLocationAccurate = isLocationAccurate(androidLocation.getAccuracy(),
                    currentSpeed);
            Log.d(TAG, "Is location accurate: " + isLocationAccurate);

			org.droidplanner.core.gcs.location.Location location = new org.droidplanner.core.gcs.location.Location(
					new Coord2D(androidLocation.getLatitude(), androidLocation.getLongitude()),
                    androidLocation.getBearing(), androidLocation.getSpeed(), isLocationAccurate);

			mLastLocation = androidLocation;
			receiver.onLocationChanged(location);
		}
	}

    private boolean isLocationAccurate(float accuracy, float currentSpeed){
        if(accuracy >= LOCATION_ACCURACY_THRESHOLD){
            Log.d(TAG, "High accuracy: " + accuracy);
            return false;
        }

        mTotalSpeed += currentSpeed;
        float avg = (mTotalSpeed / ++mSpeedReadings);

        //If moving:
        if(currentSpeed > 0){
            //if average indicates some movement
            if(avg >= 1.0){
                //Reject unreasonable updates.
                if(currentSpeed >= (avg * JUMP_FACTOR)){
                    Log.d(TAG, "High current speed: " + currentSpeed);
                    return false;
                }
            }
        }

        return true;
    }

	@Override
	public void setLocationListener(LocationReceiver receiver) {
		this.receiver = receiver;
	}
}
