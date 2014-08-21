package org.droidplanner.android.notifications;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.DroneInterfaces.Handler;
import android.content.Context;

/**
 * This class handles DroidPlanner's status bar, and audible notifications. It
 * also provides support for the Android Wear functionality.
 */
public class NotificationHandler implements DroneInterfaces.OnDroneListener {

	/**
	 * Defines the methods that need to be supported by Droidplanner's
	 * notification provider types (i.e: audible (text to speech), status bar).
	 */
	interface NotificationProvider extends DroneInterfaces.OnDroneListener {
		void quickNotify(String feedback);
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
	 * Handles Pebble notification.
	 */
	private final PebbleNotificationProvider mPebbleNotification;

	public NotificationHandler(Context context, Handler handler) {
		mTtsNotification = new TTSNotificationProvider(context, handler);
		mStatusBarNotification = new StatusBarNotificationProvider(context);
		mPebbleNotification = new PebbleNotificationProvider(context);
	}

	@Override
	public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
		mTtsNotification.onDroneEvent(event, drone);
		mStatusBarNotification.onDroneEvent(event, drone);
		mPebbleNotification.onDroneEvent(event, drone);
	}

	/**
	 * Sends a quick notification to the user. Uses toasts for written
	 * notification, and speech if voice notification is enabled.
	 *
	 * @param feedback
	 *            short message to show the user.
	 */
	public void quickNotify(String feedback) {
		mTtsNotification.quickNotify(feedback);
		mStatusBarNotification.quickNotify(feedback);
	}

	public TTSNotificationProvider getTtsNotification() {
		return mTtsNotification;
	}
}
