package org.droidplanner.android.fragments.helpers;

import android.app.Activity;
import android.support.v4.app.Fragment;

import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.api.services.DroidPlannerApi;
import org.droidplanner.android.helpers.ApiInterface;

/**
 * Provides access to the DroidPlannerApi to its derived class.
 */
public abstract class ApiListenerFragment extends Fragment implements DroidPlannerApp.ApiListener{

    private DroidPlannerApi dpApi;
    private ApiInterface.Provider apiProvider;

    protected DroidPlannerApi getApi(){
        return dpApi;
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        if(!(activity instanceof ApiInterface.Provider)){
            throw new IllegalStateException("Parent activity must implement " + ApiInterface
                    .Provider.class.getName());
        }

        apiProvider = (ApiInterface.Provider) activity;
    }

    @Override
    public void onDetach(){
        super.onDetach();
        apiProvider = null;
    }

    @Override
    public void onStart(){
        super.onStart();
        onApiConnected(apiProvider.getApi());
    }

    @Override
    public void onStop(){
        super.onStop();
        onApiDisconnected();
    }

    @Override
    public final void onApiConnected(DroidPlannerApi api) {
        if(dpApi != null || api == null) return;

        dpApi = api;
        onApiConnectedImpl(api);
    }

    protected abstract void onApiConnectedImpl(DroidPlannerApi api);

    @Override
    public final void onApiDisconnected() {
        if(dpApi == null) return;

        onApiDisconnectedImpl();
        dpApi = null;
    }

    protected abstract void onApiDisconnectedImpl();
}
