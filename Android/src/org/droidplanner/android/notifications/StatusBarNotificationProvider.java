package org.droidplanner.android.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.WearableExtender;
import android.widget.Toast;

import org.droidplanner.R;
import org.droidplanner.android.activities.FlightActivity;
import org.droidplanner.android.utils.DroidplannerPrefs;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces;

/**
 * Implements DroidPlanner's status bar notifications.
 */
public class StatusBarNotificationProvider implements NotificationHandler.NotificationProvider {

    /**
     * Android status bar's notification id.
     */
    private static final int NOTIFICATION_ID = 1;

    /**
     * Application context.
     */
    private final Context mContext;

    /**
     * Builder for the app notification.
     */
    private final NotificationCompat.Builder mNotificationBuilder;

    /**
     * Handle to the app preferences.
     */
    private final DroidplannerPrefs mAppPrefs;

    StatusBarNotificationProvider(Context context) {
        mContext = context;
        mAppPrefs = new DroidplannerPrefs(context);

        final PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
                new Intent(mContext, FlightActivity.class), 0);

        mNotificationBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.app_title))
                .setContentIntent(contentIntent);
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        switch (event) {
            case CONNECTED:
                mNotificationBuilder.setContentText(mContext.getString(R.string.connected))
                        .setOngoing(mAppPrefs.isNotificationPermanent())
                        .setSmallIcon(R.drawable.ic_launcher);
                break;

            case DISCONNECTED:
                mNotificationBuilder.setContentText(mContext.getString(R.string.disconnected))
                        .setOngoing(false)
                        .setSmallIcon(R.drawable.ic_launcher_bw);
                break;
        }

        showNotification();
    }

    /**
     * Build a notification from the notification builder, and display it.
     */
    private void showNotification(){
        NotificationManagerCompat.from(mContext).notify(NOTIFICATION_ID,
                mNotificationBuilder.build());
    }

    /**
     * Dismiss the app status bar notification.
     */
    private void dismissNotification() {
        NotificationManagerCompat.from(mContext).cancelAll();
    }


    @Override
    public void quickNotify(String feedback) {
        Toast.makeText(mContext, feedback, Toast.LENGTH_LONG).show();
    }
}
