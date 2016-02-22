package org.droidplanner.android;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;

import org.droidplanner.android.notifications.NotificationHandler;
import org.droidplanner.android.utils.NetworkUtils;

import timber.log.Timber;

/**
 * Created by Fredia Huya-Kouadio on 9/28/15.
 */
public class AppService extends Service {

    private static final IntentFilter filter = new IntentFilter();

    static {
        filter.addAction(AttributeEvent.STATE_CONNECTED);
        filter.addAction(AttributeEvent.STATE_DISCONNECTED);
        filter.addAction(AttributeEvent.AUTOPILOT_ERROR);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case AttributeEvent.STATE_CONNECTED:
                    if (notificationHandler != null)
                        notificationHandler.init();

                    if (NetworkUtils.isOnSoloNetwork(context)) {
                        bringUpCellularNetwork(context);
                    }
                    break;

                case AttributeEvent.STATE_DISCONNECTED:
                    if (notificationHandler != null) {
                        notificationHandler.terminate();
                    }

                    stopSelf();
                    break;

                case AttributeEvent.AUTOPILOT_ERROR:
                    final String errorName = intent.getStringExtra(AttributeEventExtra.EXTRA_AUTOPILOT_ERROR_ID);
                    if (notificationHandler != null)
                        notificationHandler.onAutopilotError(errorName);
                    break;
            }
        }
    };

    private final BinderHandler binderHandler = new BinderHandler();

    private NotificationHandler notificationHandler;
    private DroidPlannerApp dpApp;

    @Override
    public void onCreate() {
        super.onCreate();

        dpApp = (DroidPlannerApp) getApplication();
        dpApp.createFileStartLogging();
        final Drone drone = dpApp.getDrone();

        final Context context = getApplicationContext();
        if (NetworkUtils.isOnSoloNetwork(context)) {
            bringUpCellularNetwork(context);
        }

        notificationHandler = new NotificationHandler(context, drone);

        if (drone.isConnected()) {
            notificationHandler.init();
        }

        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);

        if (notificationHandler != null)
            notificationHandler.terminate();

        bringDownCellularNetwork();

        dpApp.closeLogFile();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binderHandler;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void bringUpCellularNetwork(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return;

        Timber.i("Setting up cellular network request.");
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkRequest networkReq = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();

        connMgr.requestNetwork(networkReq, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                Timber.i("Setting up process default network: %s", network);
                ConnectivityManager.setProcessDefaultNetwork(network);
                DroidPlannerApp.setCellularNetworkAvailability(true);
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void bringDownCellularNetwork() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return;

        Timber.i("Bringing down cellular netowrk access.");
        ConnectivityManager.setProcessDefaultNetwork(null);
        DroidPlannerApp.setCellularNetworkAvailability(false);
    }

    public static class BinderHandler extends Binder {
    }
}
