package org.droidplanner.android.glass.utils;

import android.os.Build;

/**
 * Contains glass related utility methods
 *
 * @author fhuya
 * @since 1.2.0
 */
public class GlassUtils {

    /**
     * Determines if the current device is Google Glass.
     *
     * @return true if the device is Google Glass, false otherwise.
     * @since 1.2.0
     */
    public static boolean isGlassDevice() {
        return Build.MODEL.contains("Glass");
    }

    /**
     * Private constructor to prevent instantiation.
     *
     * @since 1.2.0
     */
    private GlassUtils() {}
}
