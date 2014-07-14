package org.droidplanner.android.wear.services;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.droidplanner.android.lib.parcelables.ParcelableApmMode;
import org.droidplanner.android.lib.utils.GoogleApiClientManager;
import org.droidplanner.android.lib.utils.WearUtils;

/**
 * Handles communication with the app on the connected mobile device.
 */
public class DroidPlannerWearService extends WearableListenerService {

    private static final String TAG = DroidPlannerWearService.class.getSimpleName();

    /**
     * Action used to broadcast data updates.
     */
    public static final String ACTION_RECEIVED_DATA = DroidPlannerWearService.class.getName() +
            ".ACTION_RECEIVED_DATA";

    /**
     * Key used to retrieve the updated data bundle.
     */
    public static final String KEY_RECEIVED_DATA = "key_received_data";

    /**
     * Manager for the google api client. Handles connection/disconnection and running of
     * google api client related tasks.
     */
    private GoogleApiClientManager mGApiClientMgr;

    /**
     * Stores the data received from the main app.
     */
    private final Bundle mReceivedDataBundle = new Bundle();

    @Override
    public void onCreate() {
        super.onCreate();
        mGApiClientMgr = new GoogleApiClientManager(getApplicationContext(), Wearable.API);
        mGApiClientMgr.start();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mGApiClientMgr.stop();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        if(null != intent){
            final String action = intent.getAction();

            if(WearUtils.RESET_DRONE_FLIGHT_TIME_PATH.equals(action)){
                sendMessage(action, null);
            }
            else if(WearUtils.TOGGLE_DRONE_CONNECTION_PATH.equals(action)){
                final boolean shouldConnect = intent.getBooleanExtra(action, true);
                sendMessage(action, WearUtils.encodeDroneConnectionMsgData(shouldConnect));
            }
            else if(WearUtils.TOGGLE_DRONE_FOLLOW_ME_PATH.equals(action)){
                final boolean shouldEnable = intent.getBooleanExtra(action, false);
                sendMessage(action, WearUtils.encodeFollowMeMsgData(shouldEnable));
            }
            else if(WearUtils.SET_DRONE_FLIGHT_MODE_PATH.equals(action)){
                final ParcelableApmMode apmModeParcel = intent.getParcelableExtra(action);
                sendMessage(action, WearUtils.encodeFlightModeMsgData(apmModeParcel.getApmMode()));
            }
            else if(ACTION_RECEIVED_DATA.equals(action)){
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new
                        Intent(action).putExtra(KEY_RECEIVED_DATA, mReceivedDataBundle));
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents){
        for(DataEvent dataEvent: dataEvents){

        }
    }

    @Override
    public void onMessageReceived(MessageEvent msgEvent){
        final String msgPath = msgEvent.getPath();
        if(WearUtils.PHONE_USE_REQUIRED_PATH.equals(msgPath)){
            Toast.makeText(getApplicationContext(), "Check the main app to complete this " +
                    "action!", Toast.LENGTH_LONG).show();
        }
    }

    private void sendMessage(String msgPath, byte[] msgData){
        boolean result = WearUtils.asyncSendMessage(mGApiClientMgr, msgPath, msgData);
        if(!result){
            Log.e(TAG, "Unable to add google api client task.");
        }
    }
}
