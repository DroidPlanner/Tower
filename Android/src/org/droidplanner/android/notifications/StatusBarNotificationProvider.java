package org.droidplanner.android.notifications;

import org.droidplanner.android.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.activities.FlightActivity;
import org.droidplanner.android.utils.TextUtils;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.android.utils.unit.UnitManager;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.event.Event;
import com.o3dr.services.android.lib.drone.property.Battery;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.Home;
import com.o3dr.services.android.lib.drone.property.Signal;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.util.MathUtils;

/**
 * Implements DroidPlanner's status bar notifications.
 */
public class StatusBarNotificationProvider implements NotificationHandler.NotificationProvider {

	private static final String TAG = StatusBarNotificationProvider.class.getSimpleName();

	/**
	 * Android status bar's notification id.
	 */
	private static final int NOTIFICATION_ID = 1;

    /**
     * This is the period for the flight time update.
     */
    protected final static long FLIGHT_TIMER_PERIOD = 1000l; // 1 second

    private final Handler mHandler = new Handler();

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

    private final Drone drone;

	StatusBarNotificationProvider(Context context, Drone api) {
		mContext = context;
        this.drone = api;
		mAppPrefs = new DroidPlannerPrefs(context);

		mNotificationIntent = PendingIntent.getActivity(mContext, 0, new Intent(mContext,
				FlightActivity.class), 0);

		mToggleConnectionIntent = PendingIntent
                .getBroadcast(mContext, 0, new Intent(DroidPlannerApp.ACTION_TOGGLE_DRONE_CONNECTION), 0);

        LocalBroadcastManager.getInstance(context).registerReceiver(eventReceiver, eventFilter);
	}

    private static final IntentFilter eventFilter = new IntentFilter();
    static {
        eventFilter.addAction(Event.EVENT_CONNECTED);
        eventFilter.addAction(Event.EVENT_BATTERY);
        eventFilter.addAction(Event.EVENT_GPS);
        eventFilter.addAction(Event.EVENT_GPS_FIX);
        eventFilter.addAction(Event.EVENT_GPS_COUNT);
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

                updateFlightMode(drone);
                updateDroneState(drone);
                updateBattery(drone);
                updateGps(drone);
                updateHome(drone);
                updateRadio(drone);
            }
            else if(Event.EVENT_GPS.equals(action)){
                updateHome(drone);
            }
            else if(Event.EVENT_GPS_FIX.equals(action) || Event.EVENT_GPS_COUNT.equals(action)){
                updateGps(drone);
            }
            else if(Event.EVENT_BATTERY.equals(action)){
                updateBattery(drone);
            }
            else if(Event.EVENT_HOME.equals(action)){
                updateHome(drone);
            }
            else if(Event.EVENT_RADIO.equals(action)){
                updateRadio(drone);
            }
            else if(Event.EVENT_STATE.equals(action)){
                updateDroneState(drone);
            }
            else if(Event.EVENT_VEHICLE_MODE.equals(action)
                    || Event.EVENT_TYPE_UPDATED.equals(action)){
                updateFlightMode(drone);
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

    /**
     * Runnable used to update the drone flight time.
     */
    protected final Runnable mFlightTimeUpdater = new Runnable() {
        @Override
        public void run() {
            mHandler.removeCallbacks(this);
            if (drone == null || !drone.isConnected())
                return;

            if(mInboxBuilder != null) {
                long timeInSeconds = drone.getFlightTime();
                long minutes = timeInSeconds / 60;
                long seconds = timeInSeconds % 60;

                mInboxBuilder.setLine(2, TextUtils.normal("Air Time:   ",
                        TextUtils.bold(String.format("%02d:%02d", minutes, seconds))));
            }

            mHandler.postDelayed(this, FLIGHT_TIMER_PERIOD);
        }
    };

	private void updateRadio(Drone drone) {
		if (mInboxBuilder == null)
			return;

        Signal droneSignal = drone.getSignal();
        String update = droneSignal == null ? "--" : String.format("%d%%", MathUtils.getSignalStrength(droneSignal
                .getFadeMargin(), droneSignal.getRemFadeMargin()));
		mInboxBuilder.setLine(4, TextUtils.normal("Signal:   ",	TextUtils.bold(update)));
	}

	private void updateHome(Drone drone) {
		if (mInboxBuilder == null)
			return;

        String update = "--";
            final Gps droneGps = this.drone.getGps();
            final Home droneHome = this.drone.getHome();
            if(droneGps != null && droneGps.isValid() && droneHome != null && droneHome.isValid()) {
                double distanceToHome = MathUtils.getDistance(droneHome.getCoordinate(),
                        droneGps.getPosition());
                update = String.format("Home\n%s", UnitManager.getUnitProvider().distanceToString
                        (distanceToHome));
            }
		mInboxBuilder.setLine(0, TextUtils.normal("Home:   ", update));
	}

	private void updateGps(Drone drone) {
		if (mInboxBuilder == null)
			return;

        Gps droneGps = drone.getGps();
        String update = droneGps == null ? "--" : String.format(
                "%d, %s", droneGps.getSatellitesCount(), droneGps.getFixType());
		mInboxBuilder.setLine(1, TextUtils.normal("Satellite:   ", TextUtils.bold(update)));
	}

	private void updateBattery(Drone drone) {
		if (mInboxBuilder == null)
			return;

        Battery droneBattery = drone.getBattery();
        String update = droneBattery == null ? "--" : String.format(
                "%2.1fv (%2.0f%%)", droneBattery.getBatteryVoltage(),
                droneBattery.getBatteryRemain());

		mInboxBuilder.setLine(3, TextUtils.normal("Battery:   ", TextUtils.bold(update)));
	}

	private void updateDroneState(Drone drone) {
		if (mInboxBuilder == null)
			return;

        mHandler.removeCallbacks(mFlightTimeUpdater);
        if (drone != null && drone.isConnected()) {
            mFlightTimeUpdater.run();
        }
	}

	private void updateFlightMode(Drone drone) {
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
				Log.w(TAG, "Invalid index (" + index + ") for inbox content.");
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
