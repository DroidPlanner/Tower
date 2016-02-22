package org.droidplanner.android.fragments.control;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.FollowApi;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.gcs.follow.FollowState;

import org.droidplanner.android.fragments.SettingsFragment;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;
import org.droidplanner.android.locationrelay.LocationRelay;
import org.droidplanner.android.utils.location.CheckLocationSettings;

/**
 * Created by Fredia Huya-Kouadio on 5/25/15.
 */
public abstract class BaseFlightControlFragment extends ApiListenerFragment implements View.OnClickListener,
        FlightControlManagerFragment.SlidingUpHeader {

    static final String TAG = BaseFlightControlFragment.class.getSimpleName();

    public static final int FOLLOW_SETTINGS_UPDATE = 147;

    private static final int FOLLOW_LOCATION_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY;
    private static final long FOLLOW_LOCATION_UPDATE_INTERVAL = 30000; // ms
    private static final long FOLLOW_LOCATION_UPDATE_FASTEST_INTERVAL = 5000; // ms
    private static final float FOLLOW_LOCATION_UPDATE_MIN_DISPLACEMENT = 0; // m

    private static final IntentFilter filter = new IntentFilter(SettingsFragment.ACTION_LOCATION_SETTINGS_UPDATED);

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()){
                case SettingsFragment.ACTION_LOCATION_SETTINGS_UPDATED:
                    final int resultCode = intent.getIntExtra(SettingsFragment.EXTRA_RESULT_CODE, Activity.RESULT_OK);
                    switch (resultCode) {
                        case Activity.RESULT_OK:
                            // All required changes were successfully made. Enable follow me.
                            enableFollowMe(getDrone());
                            break;

                        case Activity.RESULT_CANCELED:
                            // The user was asked to change settings, but chose not to
                            Toast.makeText(getActivity(), "Please update your location settings!", Toast.LENGTH_LONG).show();
                            break;
                        default:
                            break;
                    }
                    break;
            }
        }
    };

    @Override
    public void onApiConnected(){
        getBroadcastManager().registerReceiver(receiver, filter);
    }

    @Override
    public void onApiDisconnected(){
        getBroadcastManager().unregisterReceiver(receiver);
    }

    protected void toggleFollowMe() {
        final Drone drone = getDrone();
        if (drone == null)
            return;

        final FollowState followState = drone.getAttribute(AttributeType.FOLLOW_STATE);
        if (followState.isEnabled()) {
            FollowApi.getApi(drone).disableFollowMe();
            LocationRelay.get().setDroneFollowing(false);
        } else {
            enableFollowMe(drone);
        }
    }

    private void enableFollowMe(final Drone drone) {
        if(drone == null)
            return;

        final LocationRequest locationReq = LocationRequest.create()
                .setPriority(FOLLOW_LOCATION_PRIORITY)
                .setFastestInterval(FOLLOW_LOCATION_UPDATE_FASTEST_INTERVAL)
                .setInterval(FOLLOW_LOCATION_UPDATE_INTERVAL)
                .setSmallestDisplacement(FOLLOW_LOCATION_UPDATE_MIN_DISPLACEMENT);

        final CheckLocationSettings locationSettingsChecker = new CheckLocationSettings(getActivity(), locationReq,
                new Runnable() {
                    @Override
                    public void run() {
                        final boolean useExternal = LocationRelay.get().isUsingExternalLocations();
                        Log.v(TAG, "useExternal=" + useExternal);

                        final FollowApi api = FollowApi.getApi(drone);
                        if(api != null) {
                            api.useExternalLocations(LocationRelay.get().isUsingExternalLocations());
                            // NOTE: This was FollowType.LEASH, but for some reason I wasn't
                            // able to select anything ELSE. It would just stop following.
                            // So now a selection from ModeFollowFragment results in the selected
                            // FollowType being updated.
                            api.enableFollowMe(LocationRelay.get().getSelectedFollowType());
                            LocationRelay.get().setDroneFollowing(true);
                        }
                        else {
                            Log.w(TAG, "No Follow API");
                        }
                    }
                });

        locationSettingsChecker.check();
    }
}
