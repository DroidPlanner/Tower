package com.o3dr.android.client;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.o3dr.android.client.utils.InstallServiceDialog;
import com.ox3dr.services.android.lib.model.IDroidPlannerServices;
import com.ox3dr.services.android.lib.model.ITLogApi;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by fhuya on 11/12/14.
 */
public class ServiceManager {

    private static final String TAG = ServiceManager.class.getSimpleName();

    private final Intent serviceIntent = new Intent(IDroidPlannerServices.class.getName());

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
        Log.d(TAG, "Connect called.");
        if(listener == null) return;

        if(isServiceConnected()) {
            listener.onServiceConnected();
        }
        else {
            if(is3DRServicesInstalled())
                connect();
            else
                promptFor3DRServicesInstall();
        }

        serviceListeners.add(listener);
    }

    public void disconnect(ServiceListener listener){
        Log.d(TAG, "Disconnect called.");
        if(listener == null) return;

        serviceListeners.remove(listener);
        listener.onServiceDisconnected();

        if(isServiceConnected() && serviceListeners.isEmpty())
            disconnect();
    }

    protected void connect(){
        if(is3DRServicesInstalled()) {
            context.bindService(serviceIntent, ox3drServicesConnection, Context.BIND_AUTO_CREATE);
        }
    }

	protected void disconnect() {
		drone.terminate();
		ox3drServices = null;
		context.unbindService(ox3drServicesConnection);

		notifyServiceDisconnected();
	}

    private boolean is3DRServicesInstalled(){
        return context.getPackageManager().resolveService(serviceIntent, 0) != null;
    }

    private void promptFor3DRServicesInstall(){
        context.startActivity(new Intent(context, InstallServiceDialog.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
