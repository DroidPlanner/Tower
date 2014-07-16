package org.droidplanner.android.wear.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.TypedValue;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Handles communication with the app on the connected mobile device.
 */
public class DroidPlannerWearService extends WearableListenerService {

    private static final String TAG = DroidPlannerWearService.class.getSimpleName();

    private static final String PACKAGE_NAME = DroidPlannerWearService.class.getPackage().getName();

    private static final int WEAR_NOTIFICATION_ID = 112;

    /**
     * Action used to broadcast data updates.
     */
    public static final String ACTION_RECEIVED_DATA = PACKAGE_NAME + ".ACTION_RECEIVED_DATA";

    /**
     * Action used to request a notification update.
     */
    public static final String ACTION_UPDATE_NOTIFICATION = PACKAGE_NAME  +
            ".ACTION_UPDATE_NOTIFICATION";

    /**
     * Action used to request cancellation of the notification.
     */
    public static final String ACTION_CANCEL_NOTIFICATION = PACKAGE_NAME +
            ".ACTION_CANCEL_NOTIFICATION";

    /**
     * Action used to broadcast drone connection updates.
     */
    public static final String ACTION_CONNECTION_UPDATE = PACKAGE_NAME + "" +
            ".ACTION_CONNECTION_UPDATE";

    /**
     * extra used to retrieve the updated data bundle.
     */
    public static final String EXTRA_RECEIVED_DATA = "extra_received_data";

    /**
     * extra used to retrieve the connection state.
     */
    public static final String EXTRA_CONNECTION_STATE = "extra_connection_state";

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
            else if(ACTION_UPDATE_NOTIFICATION.equals(action)){
                asyncUpdateNotification();
            }
            else if(ACTION_CANCEL_NOTIFICATION.equals(action)){
                cancelNotification();
            }
            else if (ACTION_RECEIVED_DATA.equals(action)) {
                runDataItemTask(WearUtils.DRONE_INFO_PATH, new DataItemTask(true) {
                    @Override
                    public void run() {
                        for(Bundle dataBundle: mDataBundleList){
                            broadcastDroneDataUpdate(dataBundle);
                        }
                    }
                });
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void runDataItemTask(final String dataPath, final DataItemTask task){
        mGApiClientMgr.addTask(mGApiClientMgr.new GoogleApiClientTask() {
            @Override
            public void run() {
                final Uri dataItemUri = new Uri.Builder().scheme(PutDataRequest
                        .WEAR_URI_SCHEME).path(dataPath).build();

                Wearable.DataApi.getDataItems(getGoogleApiClient(), dataItemUri)
                        .setResultCallback(new ResultCallback<DataItemBuffer>() {

                            @Override
                            public void onResult(DataItemBuffer dataItems) {
                                final int dataCount = dataItems.getCount();
                                final boolean hasDataMap = task.hasDataMap();

                                for(int i = 0; i < dataCount; i++){
                                    DataItem dataItem = dataItems.get(i);
                                    if(hasDataMap) {
                                        DataMap dataMap = DataMapItem.fromDataItem(dataItem)
                                                .getDataMap();
                                        if (dataMap != null) {
                                            task.addDataBundle(dataMap.toBundle());
                                        }
                                    }
                                    else{
                                        byte[] data = dataItem.getData();
                                        if(data != null){
                                            task.addData(data);
                                        }
                                    }
                                }

                                task.run();
                                dataItems.release();
                            }
                        });
            }
        });
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents){
        boolean dataUpdated = false;
        final Bundle dataBundle = new Bundle();

        for(DataEvent dataEvent: dataEvents){
            final DataItem dataItem = dataEvent.getDataItem();
            final Uri dataUri = dataItem.getUri();
            final String dataPath = dataUri.getPath();
            final int eventType = dataEvent.getType();

            if(WearUtils.DRONE_INFO_PATH.equals(dataPath)){

                if(eventType == DataEvent.TYPE_DELETED){
                    dataUpdated = true;
                }
                else if(eventType == DataEvent.TYPE_CHANGED){
                    dataBundle.putAll(DataMapItem.fromDataItem(dataItem).getDataMap().toBundle());
                    dataUpdated = true;
                }
            }
            else if(WearUtils.DRONE_CONNECTION_PATH.equals(dataPath)){
                if(eventType == DataEvent.TYPE_DELETED){
                    updateNotification(false);
                }
                else if(eventType == DataEvent.TYPE_CHANGED){
                    boolean isConnected = WearUtils.decodeDroneConnectionMsgData(dataItem.getData
                            ());
                    updateNotification(isConnected);
                }
            }
        }

        if(dataUpdated){
            broadcastDroneDataUpdate(dataBundle);
        }
    }

    private void broadcastDroneDataUpdate(Bundle dataBundle){
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new
                Intent(ACTION_RECEIVED_DATA).putExtra(EXTRA_RECEIVED_DATA, dataBundle));
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
            asyncUpdateNotification();
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

    private void asyncUpdateNotification(){
        runDataItemTask(WearUtils.DRONE_CONNECTION_PATH, new DataItemTask(false) {
            @Override
            public void run() {
                for(byte[] data: mDataList){
                    boolean isConnected = WearUtils.decodeDroneConnectionMsgData(data);
                    updateNotification(isConnected);
                }
            }
        });
    }

    private void updateNotification(boolean isConnected){
        final Context context = getApplicationContext();
        final Resources res = getResources();

        // insert a notification in the context stream
        Notification droneInfo = null;
        if(isConnected) {
            final Intent displayIntent = new Intent(context, ContextStreamActivity.class);
            final PendingIntent displayPendingIntent = PendingIntent.getActivity(context, 0,
                    displayIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            final float customHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    140, res.getDisplayMetrics());
            droneInfo = new NotificationCompat.Builder(context)
                    .setContentTitle(getText(R.string.app_title))
                    .setContentText("")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .extend(new NotificationCompat.WearableExtender()
                            .setDisplayIntent(displayPendingIntent)
                            .setCustomContentHeight((int)customHeight))
                    .build();
        }

        NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender()
                .setBackground(BitmapFactory.decodeResource(res, R.drawable.wear_notification_bg));
        if(droneInfo != null){
            extender.addPage(droneInfo);
        }

        Notification streamNotification = new NotificationCompat.Builder(context)
                .setContentTitle(getText(R.string.app_title))
                .setContentText(getText(isConnected ? R.string.connected : R.string.disconnected))
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context,
                        WearUI.class), 0))
                .extend(extender)
                .setPriority(Notification.PRIORITY_HIGH)
                .build();

        NotificationManagerCompat.from(context).notify(WEAR_NOTIFICATION_ID, streamNotification);

        //Send a broadcast as well
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent
                (ACTION_CONNECTION_UPDATE).putExtra(EXTRA_CONNECTION_STATE, isConnected));
    }

    private void cancelNotification(){
        //remove the notification from the context stream
        NotificationManagerCompat.from(getApplicationContext()).cancel(WEAR_NOTIFICATION_ID);
    }

    private abstract static class DataItemTask implements Runnable {

        protected final boolean mHasDataMap;
        protected final List<byte[]> mDataList = new ArrayList<byte[]>();
        protected final List<Bundle> mDataBundleList = new ArrayList<Bundle>();

        DataItemTask(boolean hasDataMap){
            mHasDataMap = hasDataMap;
        }

        public boolean hasDataMap(){
            return mHasDataMap;
        }

        public void addData(byte[] data){
            mDataList.add(data);
        }

        public void addDataBundle(Bundle dataBundle){
            mDataBundleList.add(dataBundle);
        }
    }
}
