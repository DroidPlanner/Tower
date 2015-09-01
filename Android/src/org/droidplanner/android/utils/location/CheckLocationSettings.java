package org.droidplanner.android.utils.location;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.o3dr.services.android.lib.util.googleApi.GoogleApiClientManager;

import org.droidplanner.android.R;
import org.droidplanner.android.activities.DrawerNavigationUI;
import org.droidplanner.android.fragments.SettingsFragment;
import org.droidplanner.android.fragments.control.BaseFlightControlFragment;

import java.lang.ref.WeakReference;

/**
 * Created by Fredia Huya-Kouadio on 5/25/15.
 */
public class CheckLocationSettings implements GoogleApiClientManager.ManagerListener {

    private static final String TAG = CheckLocationSettings.class.getSimpleName();

    private final static Api<? extends Api.ApiOptions.NotRequiredOptions>[] apisList = new Api[]{LocationServices.API};

    /**
     * Used to ensure the correct location settings are set before starting follow me.
     */
    private final GoogleApiClientManager.GoogleApiClientTask checkLocationSettings = new GoogleApiClientManager.GoogleApiClientTask() {
        @Override
        protected void doRun() {
            final GoogleApiClient googleApiClient = getGoogleApiClient();

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationReq);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());

            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult locationSettingsResult) {
                    final Status status = locationSettingsResult.getStatus();

                    final Activity activity = activityRef.get();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            if (onSuccess != null)
                                onSuccess.run();
                            break;

                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            if (activity != null) {
                                // Location settings are not satisfied. But could be fixed by showing the user
                                // a dialog.
                                try {
                                    // Show the dialog by calling startResolutionForResult(),
                                    // and check the result in onActivityResult().
                                    status.startResolutionForResult(activity, BaseFlightControlFragment.FOLLOW_SETTINGS_UPDATE);
                                } catch (IntentSender.SendIntentException e) {
                                    // Ignore the error.
                                    Log.e(TAG, e.getMessage(), e);
                                }
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            if (activity != null) {
                                // Location settings are not satisfied. However, we have no way to fix the
                                // settings so we won't show the dialog.
                                Log.w(TAG, "Unable to get accurate user location.");
                                Toast.makeText(activity, R.string.invalid_location_settings_warning, Toast.LENGTH_LONG).show();
                            }
                            break;
                    }

                    //Stop the google api client manager
                    gapiMgr.stopSafely();
                }
            });
        }
    };

    private final WeakReference<Activity> activityRef;
    private final LocationRequest locationReq;
    private final Runnable onSuccess;
    private final GoogleApiClientManager gapiMgr;

    public CheckLocationSettings(Activity activity, LocationRequest locationReq, Runnable onSuccess) {
        activityRef = new WeakReference<>(activity);
        this.locationReq = locationReq;
        this.onSuccess = onSuccess;

        gapiMgr = new GoogleApiClientManager(activity.getApplicationContext(), new Handler(), apisList);
        gapiMgr.setManagerListener(this);
    }

    public void check() {
        gapiMgr.start();
    }

    public void onReceive(Intent intent) {
        switch (intent.getAction()) {
            case SettingsFragment.ACTION_LOCATION_SETTINGS_UPDATED:
                final int resultCode = intent.getIntExtra(SettingsFragment.EXTRA_RESULT_CODE, Activity.RESULT_OK);
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made. Try to acquire user location again
                        if (onSuccess != null)
                            onSuccess.run();
                        break;

                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        final Activity activity = activityRef.get();
                        if (activity != null) {
                            Toast.makeText(activity, "Please update your location settings!", Toast.LENGTH_LONG)
                                    .show();
                        }
                        break;

                    default:
                        break;
                }
                break;
        }
    }

    @Override
    public void onGoogleApiConnectionError(ConnectionResult connectionResult) {
        final Activity activity = activityRef.get();
        if (activity == null)
            return;

        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(activity, 0);
            } catch (IntentSender.SendIntentException e) {
                //There was an error with the resolution intent. Try again.
                gapiMgr.start();
            }
        } else {
            onUnavailableGooglePlayServices(connectionResult.getErrorCode());
        }
    }

    @Override
    public void onUnavailableGooglePlayServices(int i) {
        final Activity activity = activityRef.get();
        if (activity != null) {
            GooglePlayServicesUtil.showErrorDialogFragment(i, activity, 0, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    activity.finish();
                }
            });
        }
    }

    @Override
    public void onManagerStarted() {
        gapiMgr.addTask(checkLocationSettings);
    }

    @Override
    public void onManagerStopped() {

    }
}
