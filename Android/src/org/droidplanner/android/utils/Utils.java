package org.droidplanner.android.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import org.droidplanner.android.maps.providers.DPMapProvider;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

import java.util.Locale;

/**
 * Contains application related functions.
 */
public class Utils {

    public static final String PACKAGE_NAME = "org.droidplanner.android";

    public static final String ACTION_UPDATE_OPTIONS_MENU = PACKAGE_NAME + ".UPDATE_OPTIONS_MENU";

	/**
	 * Returns the map provider selected by the user.
	 * 
	 * @param context
	 *            application context
	 * @return selected map provider
	 */
	public static DPMapProvider getMapProvider(Context context) {
		DroidPlannerPrefs prefs = new DroidPlannerPrefs(context);
		final String mapProviderName = prefs.getMapProviderName();

		return mapProviderName == null ? DPMapProvider.DEFAULT_MAP_PROVIDER : DPMapProvider
				.getMapProvider(mapProviderName);
	}

	/**
	 * Used to update the user interface language.
	 * 
	 * @param context
	 *            Application context
	 */
	public static void updateUILanguage(Context context) {
		DroidPlannerPrefs prefs = new DroidPlannerPrefs(context);
		if (prefs.isEnglishDefaultLanguage()) {
			Configuration config = new Configuration();
			config.locale = Locale.ENGLISH;

			final Resources res = context.getResources();
			res.updateConfiguration(config, res.getDisplayMetrics());
		}
	}
}
