package org.droidplanner.android.utils.analytics;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

import org.droidplanner.android.BuildConfig;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.R;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

import java.util.Map;

/**
 * Components related to google analytics logic.
 */
public class GAUtils {

	private static final String LOG_TAG = GAUtils.class.getSimpleName();

	// Not instantiable
	private GAUtils() {}

	/**
	 * List the analytics categories used in the app.
	 */
	public static class Category {
		/**
		 * Category for analytics data related to the action buttons on the
		 * flight data screen.
		 */
		public static final String FLIGHT = "Flight";

        /**
         * Category for analytics data related to the editor screen.
         */
        public static final String EDITOR = "Editor";

        /**
         * Category for measuring failsafe events.
         */
        public static final String FAILSAFE = "Failsafe";

		/**
		 * Category for analytics related to mavlink connection events.
		 */
		public static final String MAVLINK_CONNECTION = "Mavlink connection";

        /**
         * Category for droneshare analytics
         */
        public static final String DRONESHARE = "Droneshare";

        /**
         * Category for mission planning, and editing.
         */
        public static final String MISSION_PLANNING = "Mission planning";

        /**
         * Category for analytics data related to the preference dialogs.
         */
        public static final String PREFERENCE_DIALOGS = "Preference Dialogs";
	}

	/**
	 * List the custom dimension used in the app.
	 */
	public static class CustomDimension {
		/**
		 * Custom dimension used to report the used mavlink connection type.
		 */
		public static final int MAVLINK_CONNECTION_TYPE = 1;

		/**
		 * Custom dimension used to report whether the user has a droneshare
		 * account.
		 */
		public static final int DRONESHARE_ACTIVE = 2;
	}

	/**
	 * Stores a reference to the google analytics app tracker.
	 */
	private static Tracker sAppTracker;

	public static void initGATracker(DroidPlannerApp app) {
		if (sAppTracker == null) {
			final Context context = app.getApplicationContext();

			final GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);

			// Call is needed for now to allow dispatching of auto activity reports
			// (http://stackoverflow.com/a/23256722/1088814)
			analytics.enableAutoActivityReports(app);

			analytics.setAppOptOut(!DroidPlannerPrefs.getInstance(context).isUsageStatisticsEnabled());

			// If we're in debug mode, set log level to verbose.
			if (BuildConfig.DEBUG) {
				analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
			}

			sAppTracker = analytics.newTracker(R.xml.google_analytics_tracker);
		}
	}

	public static void startNewSession(Context context) {
		final DroidPlannerPrefs prefs = DroidPlannerPrefs.getInstance(context);

		final String login = prefs.getDroneshareLogin();
		final String password = prefs.getDronesharePassword();
		final boolean isDroneShareUser = prefs.isLiveUploadEnabled() && !login.isEmpty()
				&& !password.isEmpty();

		sendHit(new HitBuilders.AppViewBuilder()
				.setNewSession()
				.setCustomDimension(CustomDimension.DRONESHARE_ACTIVE,
						String.valueOf(isDroneShareUser)).build());
	}

	public static void sendEvent(HitBuilders.EventBuilder eventBuilder) {
		if (eventBuilder != null) {
			sendHit(eventBuilder.build());
		}
	}

    public static void sendEvent(HitBuilders.SocialBuilder socialBuilder){
        if(socialBuilder != null){
            sendHit(socialBuilder.build());
        }
    }

	public static void sendTiming(HitBuilders.TimingBuilder timingBuilder) {
		if (timingBuilder != null) {
			sendHit(timingBuilder.build());
		}
	}

	private static void sendHit(Map<String, String> hitParams) {
		if (sAppTracker == null) {
			Log.w(LOG_TAG, "Google Analytics tracker is not initialized.");
			return;
		}

		sAppTracker.send(hitParams);
	}
}
