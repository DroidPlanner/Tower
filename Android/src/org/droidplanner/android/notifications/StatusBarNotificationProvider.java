package org.droidplanner.android.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import org.droidplanner.R;
import org.droidplanner.android.activities.FlightActivity;
import org.droidplanner.android.activities.helpers.SuperUI;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.android.utils.TextUtils;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces;

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
     * Countdown to notification dismissal.
     */
    private static final long COUNTDOWN_TO_DISMISSAL = 60000l; //ms

    /**
     * Used to schedule notification dismissal after a disconnect event.
     */
    private final Handler mHandler = new Handler();

    /**
     * Callback used to dismiss the notification.
     */
    private final Runnable mDismissNotification = new Runnable() {
        @Override
        public void run() {
            if(mContext != null) {
                dismissNotification();
                mNotificationBuilder = null;
            }
        }
    };

    /**
     * Application context.
     */
    private final Context mContext;

    /**
     * Builder for the app notification.
     */
    private NotificationCompat.Builder mNotificationBuilder;

    /**
     * Pending intent for the notification on click behavior.
     * Opens the FlightActivity screen.
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

    StatusBarNotificationProvider(Context context) {
        mContext = context;
        mAppPrefs = new DroidPlannerPrefs(context);

        mNotificationIntent = PendingIntent.getActivity(mContext, 0,
                new Intent(mContext, FlightActivity.class), 0);

        mToggleConnectionIntent = PendingIntent.getActivity(mContext, 0,
                new Intent(mContext, FlightActivity.class).setAction(SuperUI
                        .ACTION_TOGGLE_DRONE_CONNECTION), 0);
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        boolean showNotification = true;

        switch (event) {
            case CONNECTED:
                //Cancel the notification dismissal
                mHandler.removeCallbacks(mDismissNotification);

                final String summaryText = mContext.getString(R.string.connected);

                mInboxBuilder = new InboxStyleBuilder().setSummary(summaryText);
                mNotificationBuilder = new NotificationCompat.Builder(mContext)
                        .addAction(R.drawable.ic_action_io, mContext.getText(R.string
                                .menu_disconnect), mToggleConnectionIntent)
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
                break;

            case BATTERY:
                updateBattery(drone);
                break;

            case GPS_FIX:
            case GPS_COUNT:
                updateGps(drone);
                break;

            case HOME:
                updateHome(drone);
                break;

            case RADIO:
                updateRadio(drone);
                break;

            case STATE:
                updateDroneState(drone);
                break;

            case MODE:
            case TYPE:
                updateFlightMode(drone);
                break;

            case DISCONNECTED:
                mInboxBuilder = null;

                if(mNotificationBuilder != null) {
                    mNotificationBuilder = new NotificationCompat.Builder(mContext)
                            .addAction(R.drawable.ic_action_io, mContext.getText(R.string
                                    .menu_connect), mToggleConnectionIntent)
                            .setContentIntent(mNotificationIntent)
                            .setContentTitle(mContext.getString(R.string.disconnected))
                            .setOngoing(false)
                            .setContentText("")
                            .setSmallIcon(R.drawable.ic_launcher_bw);

                    //Schedule the notification dismissal
                    mHandler.postDelayed(mDismissNotification, COUNTDOWN_TO_DISMISSAL);
                }
                break;

            default:
                showNotification = false;
                break;
        }

        if (showNotification) { showNotification(); }
    }

    private void updateRadio(Drone drone) {
        if(mInboxBuilder == null)
            return;

        mInboxBuilder.setLine(4, TextUtils.normal("Signal:   ",
                TextUtils.bold(String.format("%d%%", drone.radio.getSignalStrength()))));
    }

    private void updateHome(Drone drone) {
        if(mInboxBuilder == null)
            return;

        mInboxBuilder.setLine(0, TextUtils.normal("Home:   ", TextUtils.bold(drone.home
                .getDroneDistanceToHome().toString())));
    }

    private void updateGps(Drone drone) {
        if(mInboxBuilder == null)
            return;

        mInboxBuilder.setLine(1, TextUtils.normal("Satellite:   ",
                TextUtils.bold(String.format("%d, %s", drone.GPS.getSatCount(),
                        drone.GPS.getFixType()))
        ));
    }

    private void updateBattery(Drone drone) {
        if(mInboxBuilder == null)
            return;

        mInboxBuilder.setLine(3, TextUtils.normal("Battery:   ",
                TextUtils.bold(String.format("%2.1fv (%2.0f%%)",
                        drone.battery.getBattVolt(),
                        drone.battery.getBattRemain()))
        ));
    }

    private void updateDroneState(Drone drone) {
        if(mInboxBuilder == null)
            return;

        long timeInSeconds = drone.state.getFlightTime();
        long minutes = timeInSeconds / 60;
        long seconds = timeInSeconds % 60;

        mInboxBuilder.setLine(2, TextUtils.normal("Air Time:   ",
                TextUtils.bold(String.format("%02d:%02d", minutes, seconds))));
    }

    private void updateFlightMode(Drone drone) {
        if(mNotificationBuilder == null)
            return;

        final CharSequence modeSummary = TextUtils.normal("Flight Mode:   ",
                TextUtils.bold(drone.state.getMode().getName()));
        mNotificationBuilder.setContentTitle(modeSummary);
    }

    /**
     * Build a notification from the notification builder, and display it.
     */
    private void showNotification() {
        if (mNotificationBuilder == null) { return; }

        if(mInboxBuilder != null) {
            mNotificationBuilder.setStyle(mInboxBuilder.generateInboxStyle());
        }

        NotificationManagerCompat.from(mContext).notify(NOTIFICATION_ID, mNotificationBuilder.build());
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

        public void reset() {
            mSummary = null;
            for (int i = 0; i < MAX_LINES_COUNT; i++) {
                mLines[i] = null;
            }

            mHasContent = false;
        }

        public NotificationCompat.InboxStyle generateInboxStyle() {
            if (!mHasContent) { return null; }

            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            if (mSummary != null) { inboxStyle.setSummaryText(mSummary); }

            for (CharSequence line : mLines) {
                if (line != null) { inboxStyle.addLine(line); }
            }

            return inboxStyle;
        }
    }
}
