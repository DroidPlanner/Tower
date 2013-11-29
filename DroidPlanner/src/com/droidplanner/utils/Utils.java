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

    public static boolean isMenuDrawerLocked(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                PREF_MENU_DRAWER_LOCK, DEFAULT_MENU_DRAWER_LOCK);
    }
}
