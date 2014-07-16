package org.droidplanner.android.wear.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import org.droidplanner.android.wear.services.DroidPlannerWearService;

/**
 * Parent class for fragments that need to be notified when there's a data update.
 */
public abstract class WearFragment extends Fragment {

    private static final IntentFilter sIntentFilter = new IntentFilter(DroidPlannerWearService
            .ACTION_RECEIVED_DATA);

    private final BroadcastReceiver mDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(DroidPlannerWearService.ACTION_RECEIVED_DATA.equals(action)){
                final Bundle dataBundle = intent.getBundleExtra(DroidPlannerWearService
                        .EXTRA_RECEIVED_DATA);
                onDataUpdated(dataBundle);
            }
        }
    };

    @Override
    public void onStart(){
        super.onStart();

        final Activity activity = getActivity();
        if(activity != null){
            activity.registerReceiver(mDataReceiver, sIntentFilter);
        }
    }

    @Override
    public void onStop(){
        super.onStop();

        final Activity activity = getActivity();
        if(activity != null){
            activity.unregisterReceiver(mDataReceiver);
        }
    }

    protected abstract void onDataUpdated(Bundle dataBundle);
}
