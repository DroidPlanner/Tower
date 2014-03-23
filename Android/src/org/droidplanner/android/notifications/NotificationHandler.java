package org.droidplanner.android.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.preview.support.v4.app.NotificationManagerCompat;
import android.preview.support.wearable.notifications.WearableNotifications;
import android.support.v4.app.NotificationCompat;

import org.droidplanner.R;
import org.droidplanner.android.activities.FlightActivity;

/**
 * This class handles DroidPlanner's status bar, and audible notifications.
 * It also provides support for the Android Wear functionality.
 */
public class NotificationHandler {

    /**
     * Android notification id.
     */
    private static final int NOTIFICATION_ID = 1;

    /**
     * Dismiss the app status bar notification.
     * @param context application context
     */
    public static void dismissNotification(Context context){
        NotificationManagerCompat notificationMgr = NotificationManagerCompat.from(context);
        notificationMgr.cancelAll();
    }

    /**
     * Updates the app status bar notification with the passed text.
     * @param context application context
     * @param text notification text update
     */
    public static void updateNotification(Context context, String text){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(context.getString(R.string.app_title))
                .setContentText(text);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context,
                FlightActivity.class), 0);
        builder.setContentIntent(contentIntent);

        WearableNotifications.Builder wearableBuilder = new WearableNotifications.Builder(builder);

        NotificationManagerCompat notificationMgr = NotificationManagerCompat.from(context);
        notificationMgr.notify(NOTIFICATION_ID, wearableBuilder.build());
    }

    /**
     * Build and show the initial status bar notification.
     * @param context application context
     */
    public static void showNotification(Context context){
        updateNotification(context, context.getString(R.string.disconnected));
    }
}
