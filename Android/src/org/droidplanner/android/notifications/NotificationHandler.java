package org.droidplanner.android.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.attribute.error.ErrorType;

import org.droidplanner.android.utils.analytics.GAUtils;

/**
 * This class handles DroidPlanner's status bar, and audible notifications. It
 * also provides support for the Android Wear functionality.
 */
public class NotificationHandler {

    /**
     * Defines the methods that need to be supported by Droidplanner's
     * notification provider types (i.e: audible (text to speech), status bar).
     */
    interface NotificationProvider {
        /**
         * Release resources used by the provider.
         */
        void onTerminate();
    }

    private final static IntentFilter eventFilter = new IntentFilter();

    static {
        eventFilter.addAction(AttributeEvent.AUTOPILOT_ERROR);
    }

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case AttributeEvent.AUTOPILOT_ERROR:
                    final String errorName = intent.getStringExtra(AttributeEventExtra.EXTRA_AUTOPILOT_ERROR_ID);
                    final ErrorType errorType = ErrorType.getErrorById(errorName);
                    if (errorType != null && ErrorType.NO_ERROR != errorType) {
                        final HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                                .setCategory(GAUtils.Category.FAILSAFE)
                                .setAction("Autopilot error")
                                .setLabel(errorType.getLabel(context).toString());
                        GAUtils.sendEvent(eventBuilder);
                    }
                    break;
            }
        }
    };

    /**
     * Handles Droidplanner's audible notifications.
     */
    private final TTSNotificationProvider mTtsNotification;

    /**
     * Handles Droidplanner's status bar notification.
     */
    private final StatusBarNotificationProvider mStatusBarNotification;

    /**
     * Handles emergency beep notification.
     */
    private final EmergencyBeepNotificationProvider mBeepNotification;

    private final Context context;
    private final Drone drone;

    public NotificationHandler(Context context, Drone dpApi) {
        this.context = context;
        this.drone = dpApi;

        mTtsNotification = new TTSNotificationProvider(context, drone);
        mStatusBarNotification = new StatusBarNotificationProvider(context, drone);
        mBeepNotification = new EmergencyBeepNotificationProvider(context);

        LocalBroadcastManager.getInstance(context).registerReceiver(eventReceiver, eventFilter);
    }

    /**
     * Release resources used by the notification handler. After calling this
     * method, this object should no longer be used.
     */
    public void terminate() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(eventReceiver);
        mTtsNotification.onTerminate();
        mStatusBarNotification.onTerminate();
        mBeepNotification.onTerminate();
    }
}
