package org.droidplanner.android.utils.analytics;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

import org.droidplanner.BuildConfig;
import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.utils.DroidplannerPrefs;

import java.util.Map;

/**
 * Components related to google analytics logic.
 */
public class GAUtils {

    private static final String LOG_TAG = GAUtils.class.getSimpleName();

    //Not instantiable
    private GAUtils() {}

    /**
     * List the analytics categories used in the app.
     */
    public static enum Category {
        /**
         * Category for analytics data related to the details panel on the flight data screen.
         */
        FLIGHT_DATA_DETAILS_PANEL,

        /**
         * Category for analytics data related to the action buttons on the flight data screen.
         */
        FLIGHT_DATA_ACTION_BUTTON,

        /**
         * Category for analytics related to mavlink connection events.
         */
        MAVLINK_CONNECTION;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    /**
     * List the custom dimension used in the app.
     */
    public static enum CustomDimension {
        /**
         * Custom dimension used to report the used mavlink connection type.
         */
        MAVLINK_CONNECTION_TYPE(1),

        /**
         * Custom dimension used to report whether the user has a droneshare account.
         */
        DRONESHARE_ACTIVE(2);

        /**
         * Custom dimension index.
         */
        private int mIndex;

        private CustomDimension(int dimenIndex) {
            mIndex = dimenIndex;
        }

        /**
         * @return the custom dimension index.
         */
        public int getIndex() {
            return mIndex;
        }
    }

    /**
     * Stores a reference to the google analytics app tracker.
     */
    private static Tracker sAppTracker;

    public static void initGATracker(DroidPlannerApp app) {
        if (sAppTracker == null) {
            final Context context = app.getApplicationContext();

            final GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);

            //Call is needed for now to allow dispatching of auto activity reports
            // (http://stackoverflow.com/a/23256722/1088814)
            analytics.enableAutoActivityReports(app);

            analytics.setAppOptOut(!new DroidplannerPrefs(context).isUsageStatisticsEnabled());

            //If we're in debug mode, set log level to verbose.
            if (BuildConfig.DEBUG) {
                analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
            }

            sAppTracker = analytics.newTracker(R.xml.google_analytics_tracker);
        }
    }

    public static void startNewSession(Context context) {
        final DroidplannerPrefs prefs = new DroidplannerPrefs(context);
        final String connectionType = prefs.getMavLinkConnectionType();

        final String login = prefs.getDroneshareLogin();
        final String password = prefs.getDronesharePassword();
        final boolean isDroneShareUser = prefs.getLiveUploadEnabled() && !login.isEmpty()
                && !password.isEmpty();

        sendHit(new HitBuilders.AppViewBuilder()
                .setNewSession()
                .setCustomDimension(CustomDimension.MAVLINK_CONNECTION_TYPE.getIndex(),
                        connectionType)
                .setCustomDimension(CustomDimension.DRONESHARE_ACTIVE.getIndex(),
                        String.valueOf(isDroneShareUser))
                .build());
    }

    public static void sendEvent(HitBuilders.EventBuilder eventBuilder) {
        if (eventBuilder != null) { sendHit(eventBuilder.build()); }
    }

    public static void sendTiming(HitBuilders.TimingBuilder timingBuilder) {
        if (timingBuilder != null) { sendHit(timingBuilder.build()); }
    }

    private static void sendHit(Map<String, String> hitParams) {
        if (sAppTracker == null) {
            Log.w(LOG_TAG, "Google Analytics tracker is not initialized.");
            return;
        }

        sAppTracker.send(hitParams);
    }
}
