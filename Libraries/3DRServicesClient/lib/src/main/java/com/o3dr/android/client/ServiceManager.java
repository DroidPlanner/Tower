package com.o3dr.android.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.os.RemoteException;

import com.o3dr.android.client.interfaces.ServiceListener;
import com.o3dr.android.client.utils.InstallServiceDialog;
import com.o3dr.services.android.lib.model.IDroidPlannerServices;

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
            notifyServiceInterrupted();
        }
    };

    private final Context context;
    private ServiceListener serviceListener;
    private IDroidPlannerServices o3drServices;

    public ServiceManager(Context context){
        this.context = context;
    }

    IDroidPlannerServices get3drServices() {
        return o3drServices;
    }

    public boolean isServiceConnected() {
        try {
            return o3drServices != null && o3drServices.ping();
        } catch (RemoteException e) {
            return false;
        }
    }

    public void notifyServiceConnected(){
        if(serviceListener == null)
            return;

        serviceListener.onServiceConnected();
    }

    public void notifyServiceInterrupted(){
        if(serviceListener == null)
            return;

        serviceListener.onServiceInterrupted();
    }

    public void connect(ServiceListener listener) {
        if(serviceListener != null && isServiceConnected())
            throw new IllegalStateException("Service is already connected.");

        if(listener == null) {
            throw new IllegalArgumentException("ServiceListener argument cannot be null.");
        }

        serviceListener = listener;

        if(is3DRServicesInstalled())
            context.bindService(serviceIntent, o3drServicesConnection, Context.BIND_AUTO_CREATE);
        else
            promptFor3DRServicesInstall();
    }

	public void disconnect() {
        serviceListener = null;
        o3drServices = null;
        context.unbindService(o3drServicesConnection);
	}

    private boolean is3DRServicesInstalled(){
        final ResolveInfo info = context.getPackageManager().resolveService(serviceIntent, 0);
        if(info == null)
            return false;

        this.serviceIntent.setClassName(info.serviceInfo.packageName, info.serviceInfo.name);
        return true;
    }

    private void promptFor3DRServicesInstall(){
        context.startActivity(new Intent(context, InstallServiceDialog.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
