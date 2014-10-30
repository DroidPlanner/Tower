package org.droidplanner.android;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;

import com.three_dr.services.android.lib.model.IDroidPlannerReaderApi;
import com.three_dr.services.android.lib.model.IDroidPlannerWriterApi;

import org.droidplanner.android.api.DPCallbackApi;
import org.droidplanner.android.api.model.DPDrone;
import org.droidplanner.android.api.services.DroidPlannerApi;
import org.droidplanner.android.api.services.DroidPlannerService;
import org.droidplanner.android.communication.service.UploaderService;
import org.droidplanner.android.utils.analytics.GAUtils;
import org.droidplanner.android.utils.file.IO.ExceptionWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DroidPlannerApp extends Application {

    private static final long DELAY_TO_DISCONNECTION = 60000l; //ms

    private static final int API_UNBOUND = 0;
    private static final int API_BOUND = 1;

    public interface ApiListener {
        void onApiConnected(DroidPlannerApi api);

        void onApiDisconnected();
    }

    private final AtomicInteger apiBindingState = new AtomicInteger(API_UNBOUND);
    private final DPCallbackApi dpCallback = new DPCallbackApi(this);

    private final Runnable disconnectionTask = new Runnable() {
        @Override
        public void run() {
            if(apiBindingState.compareAndSet(API_BOUND, API_UNBOUND)) {
                notifyApiDisconnected();

                unbindService(serviceConnection);
                unbindService(readerServiceConnection);
                unbindService(writerServiceConnection);
            }
        }
    };

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            dpApi = (DroidPlannerApi) service;
            notifyApiConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            notifyApiDisconnected();
            dpApi = null;
        }
    };

    private final ServiceConnection writerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            dpWriterApi = IDroidPlannerWriterApi.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            dpWriterApi = null;
        }
    };

    private final ServiceConnection readerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            dpReaderApi = IDroidPlannerReaderApi.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            dpReaderApi = null;
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

    private DroidPlannerApi dpApi;

    private IDroidPlannerWriterApi dpWriterApi;
    private IDroidPlannerReaderApi dpReaderApi;

    private DPDrone dpDrone;

    @Override
    public void onCreate() {
        super.onCreate();
        final Context context = getApplicationContext();

        exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(dpExceptionHandler);

		GAUtils.initGATracker(this);
		GAUtils.startNewSession(context);

		// Any time the application is started, do a quick scan to see if we
		// need any uploads
		startService(UploaderService.createIntent(this));
	}

    public DroidPlannerApi getApi(){
        return dpApi;
    }

    public DPDrone getDPDrone(){
        return dpDrone;
    }

	public void addApiListener(ApiListener listener) {
		if (listener == null)
			return;

		if (dpApi != null)
			listener.onApiConnected(dpApi);

        apiListeners.add(listener);

        handler.removeCallbacks(disconnectionTask);
        if(apiBindingState.compareAndSet(API_UNBOUND, API_BOUND)){
            bindService(new Intent(getApplicationContext(), DroidPlannerService.class),
                    serviceConnection, Context.BIND_AUTO_CREATE);

            bindService(new Intent(IDroidPlannerWriterApi.class.getName()),
                    writerServiceConnection, Context.BIND_AUTO_CREATE);

            bindService(new Intent(IDroidPlannerReaderApi.class.getName()),
                    readerServiceConnection, Context.BIND_AUTO_CREATE);
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

}
