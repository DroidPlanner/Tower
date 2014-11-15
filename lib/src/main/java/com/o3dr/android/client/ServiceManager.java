package com.o3dr.android.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.o3dr.android.client.utils.InstallServiceDialog;
import com.o3dr.services.android.lib.model.IDroidPlannerServices;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by fhuya on 11/12/14.
 */
public class ServiceManager {

    private static final String TAG = ServiceManager.class.getSimpleName();

    private final Intent serviceIntent = new Intent(IDroidPlannerServices.class.getName());

    private final ServiceConnection o3drServicesConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
                o3drServices = IDroidPlannerServices.Stub.asInterface(service);
                notifyServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            disconnect();
        }
    };

    private final Set<ServiceListener> serviceListeners = new HashSet<ServiceListener>();

    private final Context context;

    private IDroidPlannerServices o3drServices;

    public ServiceManager(Context context){
        this.context = context;
    }

    IDroidPlannerServices get3drServices() {
        return o3drServices;
    }

    private void checkIfConnected(boolean connectIfDisconnected){
        if(!isServiceConnected()){
            disconnect();

            if(connectIfDisconnected)
                connect();
        }
    }

    void addServiceListener(ServiceListener listener){
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

    void removeServiceListener(ServiceListener listener){
        if(listener == null) return;

        serviceListeners.remove(listener);
        listener.onServiceDisconnected();

        if(isServiceConnected() && serviceListeners.isEmpty())
            disconnect();
    }

    void handleRemoteException(RemoteException e) {
        Log.e(TAG, e.getMessage(), e);
        checkIfConnected(false);
    }

    public boolean isServiceConnected() {
        try {
            return o3drServices != null && o3drServices.ping();
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
        addServiceListener(listener);
    }

    public void disconnect(ServiceListener listener){
        removeServiceListener(listener);
    }

    protected void connect(){
        if(is3DRServicesInstalled()) {
            context.bindService(serviceIntent, o3drServicesConnection, Context.BIND_AUTO_CREATE);
        }
    }

	protected void disconnect() {
		o3drServices = null;
		context.unbindService(o3drServicesConnection);
		notifyServiceDisconnected();
	}

    private boolean is3DRServicesInstalled(){
        return context.getPackageManager().resolveService(serviceIntent, 0) != null;
    }

    private void promptFor3DRServicesInstall(){
        context.startActivity(new Intent(context, InstallServiceDialog.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
