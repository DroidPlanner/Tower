package org.droidplanner.services.android.impl.core.gcs.location;

import android.content.Context;
import android.location.Location;
import android.os.Handler;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.o3dr.services.android.lib.util.googleApi.GoogleApiClientManager;
import com.o3dr.services.android.lib.util.googleApi.GoogleApiClientManager.GoogleApiClientTask;

import org.droidplanner.services.android.impl.core.gcs.follow.LocationRelay;
import org.droidplanner.services.android.impl.core.gcs.location.Location.LocationFinder;
import org.droidplanner.services.android.impl.core.gcs.location.Location.LocationReceiver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import timber.log.Timber;

/**
 * Feeds Location Data from Android's FusedLocation LocationProvider
 */
public class FusedLocation extends LocationCallback implements LocationFinder, GoogleApiClientManager.ManagerListener {

    private static final String TAG = FusedLocation.class.getSimpleName();

    private static final long MIN_TIME_MS = 16;
    private static final float MIN_DISTANCE_M = 0.0f;

    private final static Api<? extends Api.ApiOptions.NotRequiredOptions>[] apisList = new Api[]{LocationServices.API};

    private final GoogleApiClientManager gApiMgr;
    private final GoogleApiClientTask requestLocationUpdate;
    private boolean mLocationUpdatesEnabled = false;

    private final GoogleApiClientTask removeLocationUpdate = new GoogleApiClientTask() {
        @Override
        protected void doRun() {
            LocationServices.FusedLocationApi.removeLocationUpdates(getGoogleApiClient(),
                    FusedLocation.this);
        }
    };

    private final Map<String, LocationReceiver> receivers = new ConcurrentHashMap<>();

    private final LocationRelay locationRelay;
    private final Context context;

    public FusedLocation(Context context, final Handler handler) {
        this(context, handler, LocationRequest.PRIORITY_HIGH_ACCURACY, MIN_TIME_MS, MIN_TIME_MS, MIN_DISTANCE_M);
    }

    public FusedLocation(Context context, final Handler handler, final int locationRequestPriority,
                         final long interval, final long fastestInterval, final float smallestDisplacement) {
        this.context = context;
        this.locationRelay = new LocationRelay();

        requestLocationUpdate = new GoogleApiClientTask() {
            @Override
            protected void doRun() {
                final LocationRequest locationRequest = LocationRequest.create();
                locationRequest.setPriority(locationRequestPriority);
                locationRequest.setInterval(interval);
                locationRequest.setFastestInterval(fastestInterval);
                locationRequest.setSmallestDisplacement(smallestDisplacement);
                LocationServices.FusedLocationApi.requestLocationUpdates(getGoogleApiClient(),
                        locationRequest, FusedLocation.this, handler.getLooper());
            }
        };

        gApiMgr = new GoogleApiClientManager(context, handler, apisList);
        gApiMgr.setManagerListener(this);
    }

    @Override
    public void enableLocationUpdates(String tag, LocationReceiver receiver) {
        receivers.put(tag, receiver);
        if(!mLocationUpdatesEnabled) {
            gApiMgr.start();
            locationRelay.onFollowStart();
            mLocationUpdatesEnabled = true;
        }
    }

    @Override
    public void disableLocationUpdates(String tag) {
        if(mLocationUpdatesEnabled) {
            gApiMgr.addTask(removeLocationUpdate);
            gApiMgr.stopSafely();
            mLocationUpdatesEnabled = false;
        }
        receivers.remove(tag);
    }

    @Override
    public void onLocationAvailability(LocationAvailability locationAvailability) {
        super.onLocationAvailability(locationAvailability);

        //TODO: notify the location listener.
    }

    @Override
    public void onLocationResult(LocationResult result) {
        final Location androidLocation = result.getLastLocation();
        if (androidLocation == null)
            return;

        org.droidplanner.services.android.impl.core.gcs.location.Location gcsLocation =
                locationRelay.toGcsLocation(androidLocation);

        if(gcsLocation == null)
            return;

        Timber.d("Location Lat/Long: " + LocationRelay.getLatLongFromLocation(androidLocation));

        notifyLocationUpdate(gcsLocation);
    }

    private void notifyLocationUpdate(org.droidplanner.services.android.impl.core.gcs.location.Location location) {
        if (receivers.isEmpty()) {
            Timber.d(TAG, "notifyLocationUpdate(): No receivers");
            return;
        }

        for (LocationReceiver receiver : receivers.values()) {
            receiver.onLocationUpdate(location);
        }
    }

    @Override
    public void onGoogleApiConnectionError(ConnectionResult result) {
        notifyLocationUnavailable();

        GooglePlayServicesUtil.showErrorNotification(result.getErrorCode(), this.context);
    }

    @Override
    public void onUnavailableGooglePlayServices(int status) {
        notifyLocationUnavailable();

        GooglePlayServicesUtil.showErrorNotification(status, this.context);
    }

    private void notifyLocationUnavailable() {
        if (receivers.isEmpty())
            return;

        for (LocationReceiver listener : receivers.values()) {
            listener.onLocationUnavailable();
        }
    }

    @Override
    public void onManagerStarted() {
        gApiMgr.addTask(requestLocationUpdate);
    }

    @Override
    public void onManagerStopped() {
    }
}
