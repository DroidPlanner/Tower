package org.droidplanner.android.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.preview.support.v4.app.NotificationManagerCompat;
import android.preview.support.wearable.notifications.WearableNotifications;
import android.support.v4.app.NotificationCompat;

import org.droidplanner.R;
import org.droidplanner.android.activities.FlightActivity;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces;

/**
 * Implements DroidPlanner's status bar notifications.
 */
public class StatusBarNotificationProvider implements
		NotificationHandler.NotificationProvider {

	/**
	 * Android status bar's notification id.
	 */
	private static final int NOTIFICATION_ID = 1;

	/**
	 * Application context.
	 */
	private final Context mContext;

	StatusBarNotificationProvider(Context context) {
		mContext = context;
	}

	@Override
	public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {

	}

	/**
	 * Dismiss the app status bar notification.
	 */
	public void dismissNotification() {
		NotificationManagerCompat notificationMgr = NotificationManagerCompat
				.from(mContext);
		notificationMgr.cancelAll();
	}

	/**
	 * Updates the app status bar notification with the passed text.
	 * 
	 * @param text
	 *            notification text update
	 */
	public void updateNotification(String text) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				mContext).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(mContext.getString(R.string.app_title))
				.setContentText(text);
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
				new Intent(mContext, FlightActivity.class), 0);
		builder.setContentIntent(contentIntent);

		WearableNotifications.Builder wearableBuilder = new WearableNotifications.Builder(
				builder);

		NotificationManagerCompat notificationMgr = NotificationManagerCompat
				.from(mContext);
		notificationMgr.notify(NOTIFICATION_ID, wearableBuilder.build());
	}

	/**
	 * Build and show the initial status bar notification.
	 */
	public void showNotification() {
		updateNotification(mContext.getString(R.string.disconnected));
	}
}
