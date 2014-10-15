package org.droidplanner.android.services;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.MAVLink.Messages.ApmModes;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.droidplanner.android.lib.utils.GoogleApiClientManager;
import org.droidplanner.android.lib.utils.WearUtils;

/**
 * Manages communication with the wearable app.
 */
public class WearNotificationService extends WearableListenerService {

    private final static String TAG = WearNotificationService.class.getSimpleName();

    /**
     * Used to retrieve a DroidPlannerApi object handle.
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mDpApi = (DroidPlannerService.DroidPlannerApi) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mDpApi = null;
        }
    };

    /**
     * Handle to the droidplanner api.
     */
    private DroidPlannerService.DroidPlannerApi mDpApi;

    /**
     * Manager for the google api client. Handles connection/disconnection and running of
     * google api client related tasks.
     */
    private GoogleApiClientManager mGApiClientMgr;

    @Override
    public void onCreate() {
        super.onCreate();
        final Context context = getApplicationContext();

        mGApiClientMgr = new GoogleApiClientManager(context, Wearable.API);
        mGApiClientMgr.start();

        bindService(new Intent(context, DroidPlannerService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mGApiClientMgr.stop();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            if (WearUtils.DRONE_INFO_PATH.equals(action) ||
                    WearUtils.DRONE_STATE_PATH.equals(action)) {
                final Bundle dataBundle = intent.getBundleExtra(action);
                updateDataItem(action, dataBundle);
            }
            else if (WearUtils.MAIN_APP_STARTED_PATH.equals(action) ||
                    WearUtils.MAIN_APP_STOPPED_PATH.equals(action)) {
                boolean result = WearUtils.asyncSendMessage(mGApiClientMgr, action, null);
                if (!result) {
                    Log.e(TAG, "Unable to add google api client task.");
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateDataItem(String dataItemPath, byte[] data) {
        boolean result = WearUtils.asyncPutDataItem(mGApiClientMgr, dataItemPath, data);

        if (!result) {
            Log.e(TAG, "Unable to add google api client task.");
        }
    }

    private void updateDataItem(String dataItemPath, Bundle dataBundle) {
        boolean result = WearUtils.asyncPutDataItem(mGApiClientMgr, dataItemPath, dataBundle);

        if (!result) {
            Log.e(TAG, "Unable to add google api client task.");
        }
    }

    @Override
    public void onMessageReceived(MessageEvent msgEvent) {
        if (mDpApi != null) {
            final String msgPath = msgEvent.getPath();
            if (WearUtils.RESET_DRONE_FLIGHT_TIME_PATH.equals(msgPath)) {
                mDpApi.resetFlightTimer();
            }
            else if (WearUtils.TOGGLE_DRONE_FOLLOW_ME_PATH.equals(msgPath)) {
                boolean newState = WearUtils.decodeFollowMeMsgData(msgEvent.getData());
                if (mDpApi.isFollowMeEnabled() != newState) {
                    mDpApi.toggleFollowMe();
                }
            }
            else if (WearUtils.SET_DRONE_FLIGHT_MODE_PATH.equals(msgPath)) {
                ApmModes flightMode = WearUtils.decodeFlightModeMsgData(msgEvent.getData());
                mDpApi.setFlightMode(flightMode);
            }
            else if (WearUtils.TOGGLE_DRONE_CONNECTION_PATH.equals(msgPath)) {
                boolean shouldConnect = WearUtils.decodeDroneConnectionMsgData(msgEvent.getData());

                if (mDpApi.isDroneConnected() != shouldConnect && !mDpApi.toggleDroneConnection()) {

                    //Have the wear node(s) tell the user to check the main app.
                    boolean result = WearUtils.asyncSendMessage(mGApiClientMgr,
                            WearUtils.MAIN_APP_USE_REQUIRED_PATH, null);

                    if (!result) {
                        Log.e(TAG, "Unable to add google api client task.");
                    }
                }
            }
            else {
                Log.w(TAG, "Received message with unknown path: " + msgPath);
            }
        }
    }
}
