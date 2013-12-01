package com.droidplanner.utils;

/**
 * Contains application related constants.
 * @author fhuya
 * @since 1.2.0
 */
public class Constants {

    /**
     * Class package name.
     * @since 1.2.0
     */
    private static final String PACKAGE_NAME = Constants.class.getPackage().getName();

    /*
    Preferences, and default values.
     */
    public static final String PREF_MENU_DRAWER_LOCK = "pref_menu_drawer_lock";
    public static final boolean DEFAULT_MENU_DRAWER_LOCK = false;

    /*
    Intent actions
     */
    private static final String ACTION_PREFIX = PACKAGE_NAME + ".action.";

    public static final String ACTION_MENU_DRAWER_LOCK_UPDATE = ACTION_PREFIX +
            "MENU_DRAWER_LOCK_UPDATE";
    public static final String ACTION_CONFIGURATION_TUNING = ACTION_PREFIX +
            "CONFIGURATION_TUNING";
    public static final String ACTION_CONFIGURATION_RC = ACTION_PREFIX + "CONFIGURATION_RC";
    public static final String ACTION_CONFIGURATION_PARAMETERS = ACTION_PREFIX +
            "CONFIGURATION_PARAMETERS";
    public static final String ACTION_CONFIGURATION_SETTINGS = ACTION_PREFIX +
            "CONFIGURATION_SETTINGS";


    /*
    Bundle extras
     */
    private static final String EXTRA_PREFIX = PACKAGE_NAME + ".extra.";
    public static final String EXTRA_MENU_DRAWER_LOCK = EXTRA_PREFIX + "MENU_DRAWER_LOCK";

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private Constants(){}
}
