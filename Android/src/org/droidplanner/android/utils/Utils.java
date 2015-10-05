package org.droidplanner.android.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

import java.io.IOException;
import java.io.Reader;
import java.util.Locale;

/**
 * Contains application related functions.
 */
public class Utils {

    public static final String PACKAGE_NAME = "org.droidplanner.android";

	public static final int MIN_DISTANCE = 0; //meter
	public static final int MAX_DISTANCE = 1000; // meters

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

	public static boolean runningOnMainThread() {
		return  Looper.myLooper() == Looper.getMainLooper();
	}

	public static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public static void showDialog(DialogFragment dialog, FragmentManager fragmentManager, String tag, boolean allowStateLoss) {
		if (allowStateLoss) {
			final FragmentTransaction transaction = fragmentManager.beginTransaction();
			transaction.add(dialog, tag);
			transaction.commitAllowingStateLoss();
		} else {
			dialog.show(fragmentManager, tag);
		}

	}

	//Private constructor to prevent instantiation.
	private Utils(){}
}
