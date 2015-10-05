package org.droidplanner.android.notifications;

import android.content.Context;

import com.google.android.gms.analytics.HitBuilders;
import com.o3dr.android.client.Drone;
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
        void init();

        /**
         * Release resources used by the provider.
         */
        void onTerminate();
    }

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

    public NotificationHandler(Context context, Drone drone) {
        this.context = context;

        mTtsNotification = new TTSNotificationProvider(context, drone);
        mStatusBarNotification = new StatusBarNotificationProvider(context, drone);
        mBeepNotification = new EmergencyBeepNotificationProvider(context);
    }

    public void onAutopilotError(String errorName) {
        final ErrorType errorType = ErrorType.getErrorById(errorName);
        if (errorType != null && ErrorType.NO_ERROR != errorType) {
            final HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                    .setCategory(GAUtils.Category.FAILSAFE)
                    .setAction("Autopilot error")
                    .setLabel(errorType.getLabel(context).toString());
            GAUtils.sendEvent(eventBuilder);
        }
    }

    public void init() {
        mTtsNotification.init();
        mStatusBarNotification.init();
        mBeepNotification.init();
    }

    public void terminate() {
        mTtsNotification.onTerminate();
        mStatusBarNotification.onTerminate();
        mBeepNotification.onTerminate();
    }
}
