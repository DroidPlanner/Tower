package org.droidplanner.android.fragments.helpers;

import android.app.Activity;
import android.support.v4.app.DialogFragment;

import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.api.services.DroidPlannerApi;

/**
 * Provides access to the DroidPlannerApi to its derived class.
 */
public abstract class ApiListenerDialogFragment extends DialogFragment implements
        DroidPlannerApp.ApiListener {

    private DroidPlannerApp dpApp;

    protected DroidPlannerApi getApi(){
        return dpApp.getApi();
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        dpApp = (DroidPlannerApp) activity.getApplication();
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
