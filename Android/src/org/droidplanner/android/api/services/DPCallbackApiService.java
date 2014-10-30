package org.droidplanner.android.api.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.three_dr.services.android.lib.drone.connection.ConnectionResult;
import com.three_dr.services.android.lib.model.IDroidPlannerCallbackApi;

import java.lang.ref.WeakReference;

/**
 * Created by fhuya on 10/29/14.
 */
public class DPCallbackApiService extends Service {

    private final DPCallbackApi dpCallbackApi = new DPCallbackApi(this);

    @Override
    public IBinder onBind(Intent intent) {
        return dpCallbackApi;
    }

    public final static class DPCallbackApi extends IDroidPlannerCallbackApi.Stub {

        private final WeakReference<DPCallbackApiService> serviceRef;

        private DPCallbackApi(DPCallbackApiService service){
            serviceRef = new WeakReference<DPCallbackApiService>(service);
        }

        private DPCallbackApiService getService(){
            final DPCallbackApiService service = serviceRef.get();
            if(service == null)
                throw new IllegalStateException("Lost reference to parent service.");

            return service;
        }

        @Override
        public void onConnected(int droneId) throws RemoteException {

        }

        @Override
        public void onConnectionFailed(ConnectionResult result) throws RemoteException {

        }

        @Override
        public void onDisconnect(int droneId) throws RemoteException {

        }

        @Override
        public void onPropertyChanged(int droneId, String propertyType) throws RemoteException {

        }
    }
}
