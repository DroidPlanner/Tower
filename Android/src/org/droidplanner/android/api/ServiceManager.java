package org.droidplanner.android.api;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.ox3dr.services.android.lib.model.IDroidPlannerServices;
import com.ox3dr.services.android.lib.model.ITLogApi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by fhuya on 11/12/14.
 */
public class ServiceManager {

    private final ServiceConnection ox3drServicesConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ox3drServices = IDroidPlannerServices.Stub.asInterface(service);
            drone.start();
            notifyServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            disconnect();
        }
    };

    private final Set<ServiceListener> serviceListeners = new HashSet<ServiceListener>();

    private final Context context;

    private Drone drone;
    private IDroidPlannerServices ox3drServices;
    private ITLogApi tlogApi;

    public ServiceManager(Context context){
        this.context = context;
        drone = new Drone(context, this);
    }

    public Drone getDrone() {
        return drone;
    }

    public ITLogApi getTlogApi() {
        if (tlogApi == null) {
            try {
                tlogApi = ox3drServices.getTLogApi();
            } catch (RemoteException e) {
                return null;
            }
        }

        return tlogApi;
    }

    IDroidPlannerServices get3drServices() {
        return ox3drServices;
    }

    public boolean isServiceConnected() {
        try {
            return ox3drServices != null && ox3drServices.ping();
        } catch (RemoteException e) {
            disconnect();
            return false;
        }
    }

    public void notifyServiceConnected(){
        if(serviceListeners.isEmpty() || !isServiceConnected())
            return;

        for(ServiceListener listener: serviceListeners){
            listener.onServiceConnected();
        }
    }

    public void notifyServiceDisconnected(){
        if(serviceListeners.isEmpty() || isServiceConnected())
            return;

        for(ServiceListener listener: serviceListeners){
            listener.onServiceDisconnected();
        }
    }

    public void connect(ServiceListener listener) {
        if(listener == null) return;

        if(isServiceConnected()) {
            listener.onServiceConnected();
        }
        else {
            connect();
        }

        serviceListeners.add(listener);
    }

    public void disconnect(ServiceListener listener){
        if(listener == null) return;

        serviceListeners.remove(listener);
        listener.onServiceDisconnected();

        if(isServiceConnected() && serviceListeners.isEmpty())
            disconnect();
    }

    protected void connect(){
        context.bindService(new Intent(IDroidPlannerServices.class.getName()),
                ox3drServicesConnection, Context.BIND_AUTO_CREATE);
    }

	protected void disconnect() {
		drone.terminate();
		ox3drServices = null;
		context.unbindService(ox3drServicesConnection);

		notifyServiceDisconnected();
	}
}
