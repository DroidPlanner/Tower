package org.droidplanner.android.notifications;

import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.model.Drone;

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

        /**
         * Release resources used by the provider.
         */
        void onTerminate();
	}

    private final Drone mDrone;

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

	/**
	 * Handles emergency beep notification.
	 */
	private final EmergencyBeepNotificationProvider mBeepNotification;

	
    /**
     * Handles notifications, and data relays for connected wear nodes.
     */
    private final WearNotificationProvider mWearNotification;

	public NotificationHandler(DroidPlannerApp dpApp) {
        mDrone = dpApp.getDrone();

        final Context context = dpApp.getApplicationContext();

		mTtsNotification = new TTSNotificationProvider(context);
		mStatusBarNotification = new StatusBarNotificationProvider(context);
		mPebbleNotification = new PebbleNotificationProvider(context);
		mBeepNotification = new EmergencyBeepNotificationProvider(context);
        mWearNotification = new WearNotificationProvider(context, dpApp.followMe);

        mDrone.addDroneListener(this);
	}

	@Override
	public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
		mTtsNotification.onDroneEvent(event, drone);
		mStatusBarNotification.onDroneEvent(event, drone);
		mPebbleNotification.onDroneEvent(event, drone);
		mBeepNotification.onDroneEvent(event, drone);
        mWearNotification.onDroneEvent(event, drone);
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
        mPebbleNotification.quickNotify(feedback);
        mWearNotification.quickNotify(feedback);
	}

    /**
     * Release resources used by the notification handler.
     * After calling this method, this object should no longer be used.
     */
    public void terminate(){
        mDrone.removeDroneListener(this);
        mTtsNotification.onTerminate();
        mStatusBarNotification.onTerminate();
        mPebbleNotification.onTerminate();
        mBeepNotification.onTerminate();
        mWearNotification.onTerminate();
    }
}
