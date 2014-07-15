package org.droidplanner.android.wear.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.droidplanner.R;
import org.droidplanner.android.lib.parcelables.ParcelableApmMode;
import org.droidplanner.android.lib.utils.GoogleApiClientManager;
import org.droidplanner.android.lib.utils.WearUtils;
import org.droidplanner.android.wear.WearUI;
import org.droidplanner.android.wear.activities.ContextStreamActivity;

/**
 * Handles communication with the app on the connected mobile device.
 */
public class DroidPlannerWearService extends WearableListenerService {

    private static final String TAG = DroidPlannerWearService.class.getSimpleName();

    private static final int WEAR_NOTIFICATION_ID = 112;

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
            else if (ACTION_RECEIVED_DATA.equals(action)) {
                mGApiClientMgr.addTask(mGApiClientMgr.new GoogleApiClientTask() {
                    @Override
                    public void run() {
                        final Uri dataItemUri = new Uri.Builder().scheme(PutDataRequest
                                .WEAR_URI_SCHEME).path(WearUtils.DRONE_INFO_PATH).build();

                        Wearable.DataApi.getDataItems(getGoogleApiClient(), dataItemUri)
                                .setResultCallback(new ResultCallback<DataItemBuffer>() {

                                    @Override
                                    public void onResult(DataItemBuffer dataItems) {
                                        final Bundle dataBundle = new Bundle();
                                        final int dataCount = dataItems.getCount();

                                        for(int i = 0; i < dataCount; i++){
                                            DataItem dataItem = dataItems.get(i);
                                            DataMap dataMap = DataMapItem.fromDataItem(dataItem)
                                                    .getDataMap();
                                            if(dataMap != null){
                                                dataBundle.putAll(dataMap.toBundle());
                                            }
                                        }

                                        broadcastDroneDataUpdate(dataBundle);
                                        dataItems.release();
                                    }
                                });
                    }
                });
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents){
        boolean dataUpdated = false;
        final Bundle dataBundle = new Bundle();

        for(DataEvent dataEvent: dataEvents){
            final DataItem dataItem = dataEvent.getDataItem();
            final Uri dataUri = dataItem.getUri();
            if(WearUtils.DRONE_INFO_PATH.equals(dataUri.getPath())){
                final int eventType = dataEvent.getType();

                if(eventType == DataEvent.TYPE_DELETED){
                    dataUpdated = true;
                }
                else if(eventType == DataEvent.TYPE_CHANGED){
                    dataBundle.putAll(DataMapItem.fromDataItem(dataItem).getDataMap().toBundle());
                    dataUpdated = true;
                }
            }
        }

        if(dataUpdated){
            broadcastDroneDataUpdate(dataBundle);
        }
    }

    private void broadcastDroneDataUpdate(Bundle dataBundle){
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new
                Intent(ACTION_RECEIVED_DATA).putExtra(KEY_RECEIVED_DATA, dataBundle));
    }

    @Override
    public void onMessageReceived(MessageEvent msgEvent){
        final Context context = getApplicationContext();
        final String msgPath = msgEvent.getPath();
        if(WearUtils.MAIN_APP_USE_REQUIRED_PATH.equals(msgPath)){
            Toast.makeText(context, "Check the main app to complete this " +
                    "action!", Toast.LENGTH_LONG).show();
        }
        else if(WearUtils.MAIN_APP_STARTED_PATH.equals(msgPath)){
            updateNotification();
        }
        else if(WearUtils.MAIN_APP_STOPPED_PATH.equals(msgPath)){
            cancelNotification();
        }
    }

    private void sendMessage(String msgPath, byte[] msgData){
        boolean result = WearUtils.asyncSendMessage(mGApiClientMgr, msgPath, msgData);
        if(!result){
            Log.e(TAG, "Unable to add google api client task.");
        }
    }

    private void updateNotification(){
        final Context context = getApplicationContext();

        // insert a notification in the context stream
        final Intent displayIntent = new Intent(context, ContextStreamActivity.class);
        final PendingIntent displayPendingIntent = PendingIntent.getActivity(context, 0,
                displayIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle(getText(R.string.app_title))
                .setContentText("")
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context,
                        WearUI.class), 0))
                .setSmallIcon(R.drawable.ic_launcher)
                .extend(new NotificationCompat.WearableExtender()
                        .setDisplayIntent(displayPendingIntent))
                .build();

        NotificationManagerCompat.from(context).notify(WEAR_NOTIFICATION_ID, notification);
    }

    private void cancelNotification(){
//remove the notification from the context stream
        NotificationManagerCompat.from(getApplicationContext()).cancel(WEAR_NOTIFICATION_ID);
    }
}
