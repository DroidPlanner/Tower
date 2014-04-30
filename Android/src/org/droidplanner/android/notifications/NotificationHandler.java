package org.droidplanner.android.notifications;

import android.content.Context;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces;

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
	}

	/**
	 * Handles Droidplanner's audible notifications.
	 */
	private final TTSNotificationProvider mTtsNotification;

	/**
	 * Handles Droidplanner's status bar notification.
	 */
	private final StatusBarNotificationProvider mStatusBarNotification;

	public NotificationHandler(Context context) {
		mTtsNotification = new TTSNotificationProvider(context);
		mStatusBarNotification = new StatusBarNotificationProvider(context);
	}

	/**
	 * @return Droidplanner's audible notification provider instance.
	 */
	public TTSNotificationProvider getTtsNotificationProvider() {
		return mTtsNotification;
	}

	/**
	 * @return Droidplanner's status bar notification provider instance.
	 */
	public StatusBarNotificationProvider getStatusBarNotificationProvider() {
		return mStatusBarNotification;
	}

	@Override
	public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
		mTtsNotification.onDroneEvent(event, drone);
		mStatusBarNotification.onDroneEvent(event, drone);
	}
}
