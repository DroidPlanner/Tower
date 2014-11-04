package org.droidplanner.android;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.ox3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.ox3dr.services.android.lib.drone.connection.ConnectionType;
import com.ox3dr.services.android.lib.model.IDroidPlannerApi;
import com.ox3dr.services.android.lib.model.IDroidPlannerServices;

import org.droidplanner.android.api.DPApiCallback;
import org.droidplanner.android.communication.service.UploaderService;
import org.droidplanner.android.utils.analytics.GAUtils;
import org.droidplanner.android.utils.file.IO.ExceptionWriter;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DroidPlannerApp extends Application {

    private static final long DELAY_TO_DISCONNECTION = 60000l; //ms

    private static final String TAG = DroidPlannerApp.class.getSimpleName();

    private static final int API_UNBOUND = 0;
    private static final int API_BOUND = 1;

    public interface ApiListener {
        void onApiConnected(IDroidPlannerApi api);

        void onApiDisconnected();
    }

    private final AtomicInteger apiBindingState = new AtomicInteger(API_UNBOUND);
    private final DPApiCallback dpCallback = new DPApiCallback(this);

    private final ServiceConnection ox3drServicesConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ox3drServices = IDroidPlannerServices.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            notifyApiDisconnected();
            dpApi = null;
            ox3drServices = null;
        }
    };

    private final Runnable connectDpApiTask = new Runnable() {
        @Override
        public void run() {
            if(ox3drServices == null || dpApi != null) return;

            //Retrieve the connection parameters.
            final ConnectionParameter connParams = retrieveConnectionParameters();
            if(connParams == null){
                Log.e(TAG, "Invalid connection parameters");
                return;
            }

            try {
                dpApi = ox3drServices.connectToDrone(connParams, dpCallback);
                notifyApiConnected();
            } catch (RemoteException e) {
                Log.e(TAG, "Unable to retrieve a droidplanner api connection.", e);
            }
        }
    };

    /**
     * Used to disconnect a connected droidplanner api handle.
     */
    private final Runnable disconnectDpApiTask = new Runnable() {
        @Override
        public void run() {
            if(dpApi == null) return; //Nothing to do. It's already disconnected.

            try {
                dpApi.disconnectFromDrone();
            } catch (RemoteException e) {
                Log.e(TAG, "Error while disconnecting from the droidplanner api", e);
            }

            notifyApiDisconnected();
            dpApi = null;
        }
    };

    private final Runnable disconnectionTask = new Runnable() {
        @Override
        public void run() {
            if(apiBindingState.compareAndSet(API_BOUND, API_UNBOUND)) {
                notifyApiDisconnected();
                unbindService(ox3drServicesConnection);
            }
        }
    };

    private final Handler handler = new Handler();
    private final List<ApiListener> apiListeners = new ArrayList<ApiListener>();

    private final Thread.UncaughtExceptionHandler dpExceptionHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            new ExceptionWriter(ex).saveStackTraceToSD();
            exceptionHandler.uncaughtException(thread, ex);
        }
    };

    private Thread.UncaughtExceptionHandler exceptionHandler;

    private IDroidPlannerServices ox3drServices;
    private IDroidPlannerApi dpApi;

    private DroidPlannerPrefs dpPrefs;

    @Override
    public void onCreate() {
        super.onCreate();
        final Context context = getApplicationContext();

        dpPrefs = new DroidPlannerPrefs(context);

        exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(dpExceptionHandler);

		GAUtils.initGATracker(this);
		GAUtils.startNewSession(context);

		// Any time the application is started, do a quick scan to see if we need any uploads
		startService(UploaderService.createIntent(this));
	}

	public void addApiListener(ApiListener listener) {
		if (listener == null)
			return;

		if (dpApi != null)
			listener.onApiConnected(dpApi);

        apiListeners.add(listener);

        handler.removeCallbacks(disconnectionTask);
        if(apiBindingState.compareAndSet(API_UNBOUND, API_BOUND)){
            bindService(new Intent(IDroidPlannerServices.class.getName()),
                    ox3drServicesConnection, Context.BIND_AUTO_CREATE);
        }
	}

    public void removeApiListener(ApiListener listener){
        if(listener != null) {
            apiListeners.remove(listener);
            listener.onApiDisconnected();
        }

        if(apiListeners.isEmpty() && apiBindingState.get() != API_UNBOUND){
            //Wait a minute, then disconnect the service binding.
            handler.postDelayed(disconnectionTask, DELAY_TO_DISCONNECTION);
        }
    }

    private void notifyApiConnected(){
        if(apiListeners.isEmpty() || dpApi == null)
            return;

        for(ApiListener listener: apiListeners)
            listener.onApiConnected(dpApi);
    }

    private void notifyApiDisconnected(){
        if(apiListeners.isEmpty()) return;

        for(ApiListener listener: apiListeners)
            listener.onApiDisconnected();
    }

    public void connectToDrone(){
        handler.post(connectDpApiTask);
    }

    public void disconnectFromDrone(){
        handler.post(disconnectDpApiTask);
    }

    private ConnectionParameter retrieveConnectionParameters(){
        final int connectionType = dpPrefs.getConnectionParameterType();
        Bundle extraParams = new Bundle();

        ConnectionParameter connParams;
        switch(connectionType){
            case ConnectionType.TYPE_USB:
                connParams = new ConnectionParameter(connectionType, extraParams);
                break;

            case ConnectionType.TYPE_UDP:
                break;

            case ConnectionType.TYPE_TCP:
                break;

            case ConnectionType.TYPE_BLUETOOTH:
                break;

            default:
                Log.e(TAG, "Unrecognized connection type: " + connectionType);
                connParams = null;
                break;
        }

        return connParams;
    }
}
