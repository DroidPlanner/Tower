package com.droidplanner.glass.utils;

import android.content.Context;
import android.os.Build;
import android.preference.PreferenceManager;
import com.droidplanner.utils.Constants;

/**
 * Contains glass related utility methods
 * @author fhuya
 * @since 1.2.0
 */
public class GlassUtils {

    /**
     * Determines if the current device is Google Glass.
     * @return true if the device is Google Glass, false otherwise.
     * @since 1.2.0
     */
    public static boolean isGlassDevice(){
        return Build.MODEL.contains("Glass");
    }

    /**
     * Determines if voice should be used to drive the Google Glass user interface.
     * @param context application context
     * @return true if voice control is activated.
     */
    public static boolean isVoiceControlActive(final Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Constants
                .PREF_GLASS_VOICE_CONTROL, Constants.DEFAULT_GLASS_VOICE_CONTROL);
    }

    /**
     * Private constructor to prevent instantiation.
     * @since 1.2.0
     */
    private GlassUtils(){}
}
