package org.droidplanner.android.notifications;

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
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Battery;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.Home;
import com.o3dr.services.android.lib.drone.property.Signal;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.util.MathUtils;
import com.o3dr.services.android.lib.util.SpannableUtils;

import org.beyene.sius.unit.length.LengthUnit;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.R;
import org.droidplanner.android.activities.FlightActivity;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.android.utils.unit.UnitManager;

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

    private final Runnable removeNotification = new Runnable() {
        @Override
        public void run() {
            NotificationManagerCompat.from(mContext).cancelAll();
        }
    };

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
    }

    @Override
    public void init(){
        mHandler.removeCallbacks(removeNotification);

        final String summaryText = mContext.getString(R.string.connected);

        mInboxBuilder = new InboxStyleBuilder().setSummary(summaryText);
        mNotificationBuilder = new NotificationCompat.Builder(mContext)
                .addAction(R.drawable.ic_action_io, mContext.getText(R.string.menu_disconnect),
                        mToggleConnectionIntent)
                .setContentIntent(mNotificationIntent)
                .setContentText(summaryText)
                .setOngoing(false)
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setColor(mContext.getResources().getColor(R.color.stat_notify_connected));

        updateFlightMode(drone);
        updateDroneState(drone);
        updateBattery(drone);
        updateGps(drone);
        updateHome(drone);
        updateRadio(drone);

        showNotification();

        LocalBroadcastManager.getInstance(mContext).registerReceiver(eventReceiver, eventFilter);
    }

    /**
     * Dismiss the app status bar notification.
     */
    @Override
    public void onTerminate() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(eventReceiver);

        mInboxBuilder = null;

        if (mNotificationBuilder != null) {
            mNotificationBuilder = new NotificationCompat.Builder(mContext)
                    .addAction(R.drawable.ic_action_io,
                            mContext.getText(R.string.menu_connect), mToggleConnectionIntent)
                    .setContentIntent(mNotificationIntent)
                    .setContentTitle(mContext.getString(R.string.disconnected))
                    .setOngoing(false).setContentText("")
                    .setSmallIcon(R.drawable.ic_stat_notify);
        }

        showNotification();

        mHandler.postDelayed(removeNotification, 2000L);
    }

    private static final IntentFilter eventFilter = new IntentFilter();

    static {
        eventFilter.addAction(AttributeEvent.BATTERY_UPDATED);
        eventFilter.addAction(AttributeEvent.GPS_POSITION);
        eventFilter.addAction(AttributeEvent.GPS_FIX);
        eventFilter.addAction(AttributeEvent.GPS_COUNT);
        eventFilter.addAction(AttributeEvent.HOME_UPDATED);
        eventFilter.addAction(AttributeEvent.SIGNAL_UPDATED);
        eventFilter.addAction(AttributeEvent.STATE_UPDATED);
        eventFilter.addAction(AttributeEvent.STATE_VEHICLE_MODE);
        eventFilter.addAction(AttributeEvent.TYPE_UPDATED);
    }

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean showNotification = true;
            final String action = intent.getAction();
            switch (action) {

                case AttributeEvent.GPS_POSITION:
                    updateHome(drone);
                    break;
                case AttributeEvent.GPS_FIX:
                case AttributeEvent.GPS_COUNT:
                    updateGps(drone);
                    break;
                case AttributeEvent.BATTERY_UPDATED:
                    updateBattery(drone);
                    break;
                case AttributeEvent.HOME_UPDATED:
                    updateHome(drone);
                    break;
                case AttributeEvent.SIGNAL_UPDATED:
                    updateRadio(drone);
                    break;
                case AttributeEvent.STATE_UPDATED:
                    updateDroneState(drone);
                    break;
                case AttributeEvent.STATE_VEHICLE_MODE:
                case AttributeEvent.TYPE_UPDATED:
                    updateFlightMode(drone);
                    break;

                default:
                    showNotification = false;
                    break;
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

            if (mInboxBuilder != null) {
                long timeInSeconds = drone.getFlightTime();
                long minutes = timeInSeconds / 60;
                long seconds = timeInSeconds % 60;

                mInboxBuilder.setLine(2, SpannableUtils.normal("Air Time:   ",
                        SpannableUtils.bold(String.format("%02d:%02d", minutes, seconds))));
            }

            mHandler.postDelayed(this, FLIGHT_TIMER_PERIOD);
        }
    };

    private void updateRadio(Drone drone) {
        if (mInboxBuilder == null)
            return;

        Signal droneSignal = drone.getAttribute(AttributeType.SIGNAL);
        String update = droneSignal == null ? "--" : String.format("%d%%", MathUtils.getSignalStrength(droneSignal
                .getFadeMargin(), droneSignal.getRemFadeMargin()));
        mInboxBuilder.setLine(4, SpannableUtils.normal("Signal:   ", SpannableUtils.bold(update)));
    }

    private void updateHome(Drone drone) {
        if (mInboxBuilder == null)
            return;

        String update = "--";
        final Gps droneGps = this.drone.getAttribute(AttributeType.GPS);
        final Home droneHome = this.drone.getAttribute(AttributeType.HOME);
        if (droneGps != null && droneGps.isValid() && droneHome != null && droneHome.isValid()) {
            LengthUnit distanceToHome = UnitManager.getUnitSystem(mContext).getLengthUnitProvider()
                    .boxBaseValueToTarget(MathUtils.getDistance2D(droneHome.getCoordinate(), droneGps.getPosition()));
            update = String.format("Home\n%s", distanceToHome);
        }
        mInboxBuilder.setLine(0, SpannableUtils.normal("Home:   ", update));
    }

    private void updateGps(Drone drone) {
        if (mInboxBuilder == null)
            return;

        Gps droneGps = drone.getAttribute(AttributeType.GPS);
        String update = droneGps == null ? "--" : String.format(
                "%d, %s", droneGps.getSatellitesCount(), droneGps.getFixType());
        mInboxBuilder.setLine(1, SpannableUtils.normal("Satellite:   ", SpannableUtils.bold(update)));
    }

    private void updateBattery(Drone drone) {
        if (mInboxBuilder == null)
            return;

        Battery droneBattery = drone.getAttribute(AttributeType.BATTERY);
        String update = droneBattery == null ? "--" : String.format(
                "%2.1fv (%2.0f%%)", droneBattery.getBatteryVoltage(),
                droneBattery.getBatteryRemain());

        mInboxBuilder.setLine(3, SpannableUtils.normal("Battery:   ", SpannableUtils.bold(update)));
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

        State droneState = drone.getAttribute(AttributeType.STATE);
        VehicleMode mode = droneState == null ? null : droneState.getVehicleMode();
        String update = mode == null ? "--" : mode.getLabel();
        final CharSequence modeSummary = SpannableUtils.normal("Flight Mode:  ", SpannableUtils.bold(update));
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
