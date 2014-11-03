package org.droidplanner.android.api;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.three_dr.services.android.lib.drone.connection.ConnectionResult;
import com.three_dr.services.android.lib.model.IDroidPlannerCallbackApi;

import org.droidplanner.android.DroidPlannerApp;

import java.lang.ref.WeakReference;

/**
* Created by fhuya on 10/29/14.
*/
public final class DPCallbackApi extends IDroidPlannerCallbackApi.Stub {

    private static final String CLAZZ_NAME = DPCallbackApi.class.getName();

    public static final String ACTION_DRONE_EVENT = CLAZZ_NAME + ".ACTION_DRONE_EVENT";
    public static final String ACTION_DRONE_CONNECTION_FAILED = CLAZZ_NAME +
            ".ACTION_DRONE_CONNECTION_FAILED";

    public static final String EXTRA_CONNECTION_FAILED_ERROR_CODE =
            "extra_connection_failed_error_code";

    public static final String EXTRA_CONNECTION_FAILED_ERROR_MESSAGE =
            "extra_connection_failed_error_message";

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
        Toast.makeText(getApplication().getApplicationContext(), "Connection failed: " + result
                .getErrorMessage(), Toast.LENGTH_LONG).show();

        lbm.sendBroadcast(new Intent(ACTION_DRONE_CONNECTION_FAILED)
                .putExtra(EXTRA_CONNECTION_FAILED_ERROR_CODE, result.getErrorCode())
        .putExtra(EXTRA_CONNECTION_FAILED_ERROR_MESSAGE, result.getErrorMessage()));
    }

    @Override
    public void onDroneEvent(int droneId, String event, Bundle eventExtras) throws RemoteException {
        lbm.sendBroadcast(new Intent(ACTION_DRONE_EVENT));
        lbm.sendBroadcast(new Intent(event).putExtras(eventExtras));
    }
}
