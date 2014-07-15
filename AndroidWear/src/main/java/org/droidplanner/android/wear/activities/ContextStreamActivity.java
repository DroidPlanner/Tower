package org.droidplanner.android.wear.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

import org.droidplanner.R;
import org.droidplanner.android.lib.utils.WearUtils;
import org.droidplanner.android.wear.services.DroidPlannerWearService;

/**
 * Provides custom views for the context stream notification.
 */
public class ContextStreamActivity extends Activity {

    private static final IntentFilter sIntentFilter = new IntentFilter(DroidPlannerWearService
            .ACTION_RECEIVED_DATA);

    /**
     * Used to store, and retrieve the data bundle
     */
    private static final String EXTRA_DATA_BUNDLE = "extra_data_bundle";

    private static final boolean DEFAULT_CONNECTION_STATE = false;

    private final BroadcastReceiver mDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(DroidPlannerWearService.ACTION_RECEIVED_DATA.equals(action)){
                //update the ui.
                mDataBundle = intent.getBundleExtra(DroidPlannerWearService.KEY_RECEIVED_DATA);
                refreshUI();
            }
        }
    };

    private Bundle mDataBundle;
    private View mDisconnectedView;
    private View mConnectedView;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_context_stream);

        mDisconnectedView = findViewById(R.id.context_stream_disconnected);
        mConnectedView = findViewById(R.id.context_stream_connected);

        if(savedInstanceState != null){
            mDataBundle = savedInstanceState.getBundle(EXTRA_DATA_BUNDLE);
            refreshUI();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outstate){
        super.onSaveInstanceState(outstate);

        if(mDataBundle != null){
            outstate.putBundle(EXTRA_DATA_BUNDLE, mDataBundle);
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        final Context context = getApplicationContext();

        LocalBroadcastManager.getInstance(context).registerReceiver(mDataReceiver, sIntentFilter);

        //Request data update from the service.
        startService(new Intent(context, DroidPlannerWearService.class).setAction
                (DroidPlannerWearService.ACTION_RECEIVED_DATA));
    }

    @Override
    public void onStop(){
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mDataReceiver);
    }

    private void refreshUI(){
        if(mDataBundle == null || !mDataBundle.getBoolean(WearUtils.KEY_DRONE_CONNECTION_STATE,
                DEFAULT_CONNECTION_STATE)){
            mConnectedView.setVisibility(View.GONE);
            mDisconnectedView.setVisibility(View.VISIBLE);
        }
        else{
            mConnectedView.setVisibility(View.VISIBLE);
            mDisconnectedView.setVisibility(View.GONE);
        }
    }
}
