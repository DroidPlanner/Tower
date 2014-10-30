package org.droidplanner.android.api;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;

import com.three_dr.services.android.lib.drone.connection.ConnectionResult;
import com.three_dr.services.android.lib.model.IDroidPlannerCallbackApi;

import org.droidplanner.android.DroidPlannerApp;

import java.lang.ref.WeakReference;

/**
* Created by fhuya on 10/29/14.
*/
public final class DPCallbackApi extends IDroidPlannerCallbackApi.Stub {

    private final WeakReference<DroidPlannerApp> appRef;
    private final LocalBroadcastManager lbm;

    public DPCallbackApi(DroidPlannerApp app){
        appRef = new WeakReference<DroidPlannerApp>(app);
        lbm = LocalBroadcastManager.getInstance(app.getApplicationContext());
    }

    private DroidPlannerApp getApplication(){
        final DroidPlannerApp app = appRef.get();
        if(app == null)
            throw new IllegalStateException("Lost reference to parent service.");

        return app;
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) throws RemoteException {

    }

    @Override
    public void onDroneEvent(int droneId, String event, Bundle eventExtras) throws RemoteException {
        lbm.sendBroadcast((new Intent(event).putExtras(eventExtras)));
    }
}
