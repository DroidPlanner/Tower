package org.droidplanner.android.fragments.helpers;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;

import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.api.model.DPDrone;
import org.droidplanner.android.api.services.DroidPlannerApi;

/**
 * Provides access to the DroidPlannerApi to its derived class.
 */
public abstract class ApiListenerFragment extends Fragment implements DroidPlannerApp.ApiListener{

    private DroidPlannerApp dpApp;
    private LocalBroadcastManager broadcastManager;

    protected DroidPlannerApi getApi(){
        return dpApp.getApi();
    }

    protected DPDrone getDPDrone(){
        return dpApp.getDPDrone();
    }

    protected LocalBroadcastManager getBroadcastManager(){
        return broadcastManager;
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        dpApp = (DroidPlannerApp) activity.getApplication();
        broadcastManager = LocalBroadcastManager.getInstance(activity.getApplicationContext());
    }

    @Override
    public void onStart(){
        super.onStart();
        dpApp.addApiListener(this);
    }

    @Override
    public void onStop(){
        super.onStop();
        dpApp.removeApiListener(this);
    }
}
