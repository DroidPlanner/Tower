package org.droidplanner.android.utils.prefs;

/**
 * Map auto pan target types.
 */
public enum AutoPanMode {
    USER,
    DRONE,
    DISABLED;

    public static final String PREF_KEY = "pref_auto_pan_mode";
}
