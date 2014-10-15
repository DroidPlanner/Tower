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

import com.MAVLink.Messages.ApmModes;
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
import org.droidplanner.android.lib.utils.ParcelableUtils;
import org.droidplanner.android.lib.utils.WearUtils;
import org.droidplanner.android.wear.activities.ContextStreamActivity;
import org.droidplanner.android.wear.activities.FlightModesSelectionActivity;

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
    public static final String ACTION_DRONE_STATE_UPDATE = PACKAGE_NAME + "" +
            ".ACTION_DRONE_STATE_UPDATE";

    /**
     * extra used to retrieve the updated data bundle.
     */
    public static final String EXTRA_RECEIVED_DATA = "extra_received_data";

    /**
     * extra used to retrieve the connection state.
     */
    public static final String EXTRA_DRONE_STATE = "extra_drone_state";
    public static final String EXTRA_CURRENT_FLIGHT_MODE = "extra_current_flight_mode";

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
            public void doRun() {
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
                                dataItems.close();
                            }
                        });
            }
        });
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents){
        boolean dataUpdated = false;
        final Bundle dataBundle = new Bundle();

        boolean droneStateUpdated = false;
        final Bundle droneStateBundle = new Bundle();

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
            else if(WearUtils.DRONE_STATE_PATH.equals(dataPath)){
                if(eventType == DataEvent.TYPE_DELETED){
                    droneStateUpdated = true;
                }
                else if(eventType == DataEvent.TYPE_CHANGED){
                    droneStateBundle.putAll(DataMapItem.fromDataItem(dataItem).getDataMap().toBundle());
                    droneStateUpdated = true;
                }
            }
        }

        if(droneStateUpdated){
            updateDroneState(droneStateBundle);
        }

        if(dataUpdated){
            broadcastDroneDataUpdate(dataBundle);
        }

        dataEvents.close();
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
        runDataItemTask(WearUtils.DRONE_STATE_PATH, new DataItemTask(true) {
            @Override
            public void run() {
                for(Bundle dataBundle: mDataBundleList){
                    updateDroneState(dataBundle);
                }
            }
        });
    }

    private void updateDroneState(Bundle droneState){
        final Context context = getApplicationContext();
        final Resources res = getResources();

        boolean isConnected = droneState.getBoolean(WearUtils.KEY_DRONE_CONNECTION_STATE, false);
        boolean isFollowMeEnabled = droneState.getBoolean(WearUtils.KEY_DRONE_FOLLOW_STATE, false);

        byte[] apmModeBytes = droneState.getByteArray(WearUtils.KEY_DRONE_FLIGHT_MODE);
        ParcelableApmMode apmMode = null;
        if(apmModeBytes != null) {
            apmMode = ParcelableUtils.unmarshall(apmModeBytes, ParcelableApmMode.CREATOR);
        }

        if(apmMode == null || apmMode.getApmMode() == null){
            apmMode = new ParcelableApmMode(ApmModes.UNKNOWN);
        }


        // insert a notification in the context stream
        NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender()
                .setBackground(BitmapFactory.decodeResource(res, R.drawable.wear_notification_bg));

        /*
        Set of actions
         */
        //Connection action
        final CharSequence connectTitle = getText(isConnected ? R.string.menu_disconnect : R
                .string.menu_connect);
        final Intent connectIntent = new Intent(context, DroidPlannerWearService.class)
                .setAction(WearUtils.TOGGLE_DRONE_CONNECTION_PATH)
                .putExtra(WearUtils.TOGGLE_DRONE_CONNECTION_PATH, !isConnected);
        final PendingIntent connectPendingIntent = PendingIntent.getService(context, 0,
                connectIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        final NotificationCompat.Action connectAction = new NotificationCompat.Action(R.drawable
                .ic_action_io, connectTitle, connectPendingIntent);

        extender.addAction(connectAction);

        int notificationPriority = Notification.PRIORITY_DEFAULT;
        if(isConnected) {
            notificationPriority = Notification.PRIORITY_MAX;

            final Intent displayIntent = new Intent(context, ContextStreamActivity.class);
            final PendingIntent displayPendingIntent = PendingIntent.getActivity(context, 0,
                    displayIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            final float customHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    140, res.getDisplayMetrics());
            Notification droneInfo = new NotificationCompat.Builder(context)
                    .setContentTitle(getText(R.string.app_title))
                    .setContentText("")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .extend(new NotificationCompat.WearableExtender()
                            .setDisplayIntent(displayPendingIntent)
                            .setCustomContentHeight((int)customHeight))
                    .build();

            extender.addPage(droneInfo);

            //Flight mode action
            final CharSequence flightModeTitle = apmMode.getApmMode().getName();
            final Intent flightModeIntent = new Intent(context, FlightModesSelectionActivity.class)
                    .putExtra(EXTRA_CURRENT_FLIGHT_MODE, apmMode);
            final PendingIntent flightModePendingIntent = PendingIntent.getActivity(context, 0,
                    flightModeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            final NotificationCompat.Action flightModeAction = new NotificationCompat.Action(R
                    .drawable.ic_action_plane_white, flightModeTitle, flightModePendingIntent);

            extender.addAction(flightModeAction);

            //Follow me toggle action
            final CharSequence followTitle = String.format("%s Follow-Me",
                    isFollowMeEnabled ? "Disable" : "Enable");
            final Intent followIntent = new Intent(context, DroidPlannerWearService.class)
                    .setAction(WearUtils.TOGGLE_DRONE_FOLLOW_ME_PATH)
                    .putExtra(WearUtils.TOGGLE_DRONE_FOLLOW_ME_PATH, !isFollowMeEnabled);
            final PendingIntent followPendingIntent = PendingIntent.getService(context, 0,
                    followIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            final NotificationCompat.Action followAction = new NotificationCompat.Action(R.drawable
                    .ic_follow, followTitle, followPendingIntent);

            extender.addAction(followAction);
        }

        final CharSequence notificationText = getText(isConnected ? R.string.connected : R.string
                .disconnected);
        Notification streamNotification = new NotificationCompat.Builder(context)
                .setContentTitle(getText(R.string.app_title))
                .setContentText(notificationText)
                .setSmallIcon(R.drawable.ic_launcher)
                .extend(extender)
                .setPriority(notificationPriority)
                .setOngoing(isConnected)
                .setTicker(notificationText)
                .setDefaults(Notification.DEFAULT_ALL)
                .build();

        NotificationManagerCompat.from(context).notify(WEAR_NOTIFICATION_ID, streamNotification);

        //Send a broadcast as well
        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(new Intent(ACTION_DRONE_STATE_UPDATE)
                        .putExtra(EXTRA_DRONE_STATE, droneState));
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
