package com.droidplanner.utils;

import android.content.Context;
import android.preference.PreferenceManager;

import static com.droidplanner.utils.Constants.DEFAULT_MENU_DRAWER_LOCK;
import static com.droidplanner.utils.Constants.PREF_MENU_DRAWER_LOCK;

/**
 * Contains application related functions.
 * @author fhuya
 * @since 1.2.0
 */
public class Utils {

    /**
     * Gets the user preference regarding the state of the menu drawer.
     * @param context application context
     * @return true if the menu drawer should be locked open, false otherwise.
     * @since 1.2.0
     */
    public static boolean isMenuDrawerLocked(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                PREF_MENU_DRAWER_LOCK, DEFAULT_MENU_DRAWER_LOCK);
    }
}
