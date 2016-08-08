package org.droidplanner.android.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

import java.io.IOException;
import java.io.Reader;
import java.util.Locale;
import java.util.Set;

import timber.log.Timber;

/**
 * Contains application related functions.
 */
public class Utils {

    public static final String PACKAGE_NAME = "org.droidplanner.android";

	public static final int MIN_DISTANCE = 0; //meter
	public static final int MAX_DISTANCE = 1000; // meters
	public static final int MAX_RADIUS = 255; //meters, should be used with mission items which implement NAV_LOITER_TURNS

	public static final int INVALID_APP_VERSION_CODE = -1;

	/**
	 * Used to update the user interface language.
	 * 
	 * @param context
	 *            Application context
	 */
	public static void updateUILanguage(Context context) {
		DroidPlannerPrefs prefs = DroidPlannerPrefs.getInstance(context);
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

	public static int getAppVersionCode(Context context){
		int versionCode = INVALID_APP_VERSION_CODE;
		try {
			versionCode = context.getPackageManager()
                .getPackageInfo(context.getPackageName(), 0).versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			Timber.e(e, "Unable to retrieve the app version code.");
		}

		return versionCode;
	}

	/**
	 * Check if two bundle objects are equals
	 * @param one
	 * @param two
     * @return
     */
	public static boolean equalBundles(Bundle one, Bundle two) {
		if(one == two)
			return true;

		if(one == null || two == null)
			return false;

		if(one.size() != two.size())
			return false;

		Set<String> setOne = one.keySet();
		Object valueOne;
		Object valueTwo;

		for(String key : setOne) {
			valueOne = one.get(key);
			valueTwo = two.get(key);
			if(valueOne instanceof Bundle && valueTwo instanceof Bundle &&
					!equalBundles((Bundle) valueOne, (Bundle) valueTwo)) {
				return false;
			}
			else if(valueOne == null) {
				if(valueTwo != null || !two.containsKey(key))
					return false;
			}
			else if(!valueOne.equals(valueTwo))
				return false;
		}

		return true;
	}

	//Private constructor to prevent instantiation.
	private Utils(){}
}
