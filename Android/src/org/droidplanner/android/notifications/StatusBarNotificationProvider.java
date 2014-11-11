package org.droidplanner.android.notifications;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.activities.FlightActivity;
import org.droidplanner.android.api.DroneApi;
import org.droidplanner.android.utils.TextUtils;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.android.utils.unit.UnitManager;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ox3dr.services.android.lib.drone.event.Event;
import com.ox3dr.services.android.lib.drone.property.Battery;
import com.ox3dr.services.android.lib.drone.property.Gps;
import com.ox3dr.services.android.lib.drone.property.Home;
import com.ox3dr.services.android.lib.drone.property.Signal;
import com.ox3dr.services.android.lib.drone.property.State;
import com.ox3dr.services.android.lib.drone.property.VehicleMode;
import com.ox3dr.services.android.lib.util.MathUtils;

/**
 * Implements DroidPlanner's status bar notifications.
 */
public class StatusBarNotificationProvider implements NotificationHandler.NotificationProvider {

	private static final String LOG_TAG = StatusBarNotificationProvider.class.getSimpleName();

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
	private NotificationCompat.Builder mNotificationBuilder;

	/**
	 * Pending intent for the notification on click behavior. Opens the
	 * FlightActivity screen.
	 */
	private final PendingIntent mNotificationIntent;

	/**
	 * Pending intent for the notification connect/disconnect action.
	 */
	private final PendingIntent mToggleConnectionIntent;

	/**
	 * Uses to generate the inbox style use to populate the notification.
	 */
	private InboxStyleBuilder mInboxBuilder;

	/**
	 * Handle to the app preferences.
	 */
	private final DroidPlannerPrefs mAppPrefs;

    private final DroneApi droneApi;

	StatusBarNotificationProvider(Context context, DroneApi api) {
		mContext = context;
        this.droneApi = api;
		mAppPrefs = new DroidPlannerPrefs(context);

		mNotificationIntent = PendingIntent.getActivity(mContext, 0, new Intent(mContext,
				FlightActivity.class), 0);

		mToggleConnectionIntent = PendingIntent
                .getBroadcast(mContext, 0, new Intent(mContext, DroneApi.class)
                        .setAction(DroneApi.ACTION_TOGGLE_DRONE_CONNECTION), 0);

        LocalBroadcastManager.getInstance(context).registerReceiver(eventReceiver, eventFilter);
	}

    private static final IntentFilter eventFilter = new IntentFilter();
    static {
        eventFilter.addAction(Event.EVENT_CONNECTED);
        eventFilter.addAction(Event.EVENT_BATTERY);
        eventFilter.addAction(Event.EVENT_GPS);
        eventFilter.addAction(Event.EVENT_GPS_STATE);
        eventFilter.addAction(Event.EVENT_HOME);
        eventFilter.addAction(Event.EVENT_RADIO);
        eventFilter.addAction(Event.EVENT_STATE);
        eventFilter.addAction(Event.EVENT_VEHICLE_MODE);
        eventFilter.addAction(Event.EVENT_TYPE_UPDATED);
        eventFilter.addAction(Event.EVENT_DISCONNECTED);
    }

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean showNotification = true;
            final String action = intent.getAction();
            if(Event.EVENT_CONNECTED.equals(action)){
                final String summaryText = mContext.getString(R.string.connected);

                mInboxBuilder = new InboxStyleBuilder().setSummary(summaryText);
                mNotificationBuilder = new NotificationCompat.Builder(mContext)
                        .addAction(R.drawable.ic_action_io, mContext.getText(R.string.menu_disconnect),
                                mToggleConnectionIntent)
                        .setContentIntent(mNotificationIntent)
                        .setContentText(summaryText)
                        .setOngoing(mAppPrefs.isNotificationPermanent())
                        .setSmallIcon(R.drawable.ic_launcher);

                updateFlightMode(droneApi);
                updateDroneState(droneApi);
                updateBattery(droneApi);
                updateGps(droneApi);
                updateHome(droneApi);
                updateRadio(droneApi);
            }
            else if(Event.EVENT_GPS.equals(action)){
                updateHome(droneApi);
            }
            else if(Event.EVENT_GPS_STATE.equals(action)){
                updateGps(droneApi);
            }
            else if(Event.EVENT_BATTERY.equals(action)){
                updateBattery(droneApi);
            }
            else if(Event.EVENT_HOME.equals(action)){
                updateHome(droneApi);
            }
            else if(Event.EVENT_RADIO.equals(action)){
                updateRadio(droneApi);
            }
            else if(Event.EVENT_STATE.equals(action)){
                updateDroneState(droneApi);
            }
            else if(Event.EVENT_VEHICLE_MODE.equals(action)
                    || Event.EVENT_TYPE_UPDATED.equals(action)){
                updateFlightMode(droneApi);
            }
            else if(Event.EVENT_DISCONNECTED.equals(action)){
                mInboxBuilder = null;

                if (mNotificationBuilder != null) {
                    mNotificationBuilder = new NotificationCompat.Builder(mContext)
                            .addAction(R.drawable.ic_action_io,
                                    mContext.getText(R.string.menu_connect), mToggleConnectionIntent)
                            .setContentIntent(mNotificationIntent)
                            .setContentTitle(mContext.getString(R.string.disconnected))
                            .setOngoing(false).setContentText("")
                            .setSmallIcon(R.drawable.ic_launcher_bw);
                }
            }
            else{
                showNotification = false;
            }

            if (showNotification) {
                showNotification();
            }
        }
    };

	private void updateRadio(DroneApi drone) {
		if (mInboxBuilder == null)
			return;

        Signal droneSignal = drone.getSignal();
        String update = droneSignal == null ? "--" : String.format("%d%%", MathUtils.getSignalStrength(droneSignal
                .getFadeMargin(), droneSignal.getRemFadeMargin()));
		mInboxBuilder.setLine(4, TextUtils.normal("Signal:   ",	TextUtils.bold(update)));
	}

	private void updateHome(DroneApi drone) {
		if (mInboxBuilder == null)
			return;

        String update = "--";
            final Gps droneGps = droneApi.getGps();
            final Home droneHome = droneApi.getHome();
            if(droneGps != null && droneGps.isValid() && droneHome != null && droneHome.isValid()) {
                double distanceToHome = MathUtils.getDistance(droneHome.getCoordinate(),
                        droneGps.getPosition());
                update = String.format("Home\n%s", UnitManager.getUnitProvider().distanceToString
                        (distanceToHome));
            }
		mInboxBuilder.setLine(0, TextUtils.normal("Home:   ", update));
	}

	private void updateGps(DroneApi drone) {
		if (mInboxBuilder == null)
			return;

        Gps droneGps = drone.getGps();
        String update = droneGps == null ? "--" : String.format(
                "%d, %s", droneGps.getSatellitesCount(), droneGps.getFixType());
		mInboxBuilder.setLine(1, TextUtils.normal("Satellite:   ", TextUtils.bold(update)));
	}

	private void updateBattery(DroneApi drone) {
		if (mInboxBuilder == null)
			return;

        Battery droneBattery = drone.getBattery();
        String update = droneBattery == null ? "--" : String.format(
                "%2.1fv (%2.0f%%)", droneBattery.getBatteryVoltage(),
                droneBattery.getBatteryRemain());

		mInboxBuilder.setLine(3, TextUtils.normal("Battery:   ", TextUtils.bold(update)));
	}

	private void updateDroneState(DroneApi drone) {
		if (mInboxBuilder == null)
			return;

		long timeInSeconds = drone.getFlightTime();
		long minutes = timeInSeconds / 60;
		long seconds = timeInSeconds % 60;

		mInboxBuilder.setLine(2, TextUtils.normal("Air Time:   ",
                TextUtils.bold(String.format("%02d:%02d", minutes, seconds))));
	}

	private void updateFlightMode(DroneApi drone) {
		if (mNotificationBuilder == null)
			return;

        State droneState = drone.getState();
        VehicleMode mode = droneState == null ? null : droneState.getVehicleMode();
        String update = mode == null ? "--" : mode.getLabel();
		final CharSequence modeSummary = TextUtils.normal("Flight Mode:  ", TextUtils.bold(update));
		mNotificationBuilder.setContentTitle(modeSummary);
	}

	/**
	 * Build a notification from the notification builder, and display it.
	 */
	private void showNotification() {
		if (mNotificationBuilder == null) {
			return;
		}

		if (mInboxBuilder != null) {
			mNotificationBuilder.setStyle(mInboxBuilder.generateInboxStyle());
		}

		NotificationManagerCompat.from(mContext).notify(NOTIFICATION_ID,
                mNotificationBuilder.build());
	}

	/**
	 * Dismiss the app status bar notification.
	 */
    @Override
	public void onTerminate() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(eventReceiver);
		NotificationManagerCompat.from(mContext).cancelAll();
	}

	private static class InboxStyleBuilder {
		private static final int MAX_LINES_COUNT = 5;

		private final CharSequence[] mLines = new CharSequence[MAX_LINES_COUNT];

		private CharSequence mSummary;

		private boolean mHasContent = false;

		public void setLine(int index, CharSequence content) {
			if (index >= mLines.length || index < 0) {
				Log.w(LOG_TAG, "Invalid index (" + index + ") for inbox content.");
				return;
			}

			mLines[index] = content;
			mHasContent = true;
		}

		public InboxStyleBuilder setSummary(CharSequence summary) {
			mSummary = summary;
			mHasContent = true;
			return this;
		}

		public NotificationCompat.InboxStyle generateInboxStyle() {
			if (!mHasContent) {
				return null;
			}

			NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
			if (mSummary != null) {
				inboxStyle.setSummaryText(mSummary);
			}

			for (CharSequence line : mLines) {
				if (line != null) {
					inboxStyle.addLine(line);
				}
			}

			return inboxStyle;
		}
	}
}
