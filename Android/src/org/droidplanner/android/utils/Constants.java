package org.droidplanner.android.utils;

/**
 * Contains application related constants.
 */
public class Constants {

    /**
     * Class package name.
     */
    public static final String PACKAGE_NAME = Constants.class.getPackage().getName();

    /*
     * Preferences, and default values.
     */
    public static final String PREF_BLUETOOTH_DEVICE_ADDRESS = "pref_bluetooth_device_address";

    /**
     * Sets whether or not the default language for the ui should be english.
     */
    public static final String PREF_UI_LANGUAGE = "pref_ui_language_english";

    /**
     * By default, the system language should be used for the ui.
     */
    public static final boolean DEFAULT_PREF_UI_LANGUAGE = false;

    /**
     * Preference key for the drone settings' category.
     */
    public static final String PREF_DRONE_SETTINGS = "pref_drone_settings";

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private Constants() {}
}
