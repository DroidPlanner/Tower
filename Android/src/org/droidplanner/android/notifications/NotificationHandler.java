package org.droidplanner.android.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.analytics.HitBuilders;
import com.o3dr.android.client.Drone;
import com.ox3dr.services.android.lib.drone.event.Event;

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

	private final static IntentFilter eventFilter = new IntentFilter(Event.EVENT_AUTOPILOT_FAILSAFE);

	private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (Event.EVENT_AUTOPILOT_FAILSAFE.equals(action)) {
				final HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
						.setCategory(GAUtils.Category.FAILSAFE).setAction("Autopilot warning")
						.setLabel(drone.getState().getFailsafeWarning());
				GAUtils.sendEvent(eventBuilder);
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
	 * Handles Pebble notification.
	 */
	private final PebbleNotificationProvider mPebbleNotification;

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
		mPebbleNotification = new PebbleNotificationProvider(context, dpApi);
		mBeepNotification = new EmergencyBeepNotificationProvider(context);

		LocalBroadcastManager.getInstance(context).registerReceiver(eventReceiver, eventFilter);
	}

	/**
	 * Release resources used by the notification handler. After calling this
	 * method, this object should no longer be used.
	 */
	public void terminate() {
		mTtsNotification.onTerminate();
		mStatusBarNotification.onTerminate();
		mPebbleNotification.onTerminate();
		mBeepNotification.onTerminate();

		LocalBroadcastManager.getInstance(context).unregisterReceiver(eventReceiver);
	}
}
