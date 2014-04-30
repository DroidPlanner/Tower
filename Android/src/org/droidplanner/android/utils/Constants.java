package org.droidplanner.android.utils;

import org.droidplanner.android.utils.Utils.ConnectionType;

/**
 * Contains application related constants.
 * 
 * @author fhuya
 * @since 1.2.0
 */
public class Constants {

	/**
	 * Class package name.
	 * 
	 * @since 1.2.0
	 */
	private static final String PACKAGE_NAME = Constants.class.getPackage()
			.getName();

	/*
	 * Preferences, and default values.
	 */
	public static final String PREF_BLUETOOTH_DEVICE_ADDRESS = "pref_bluetooth_device_address";

	/**
	 * This is the preference for the connection type. The possible values are
	 * members of the
	 * {@link org.droidplanner.android.utils.Utils.ConnectionType} enum.
	 * 
	 * @since 1.2.0
	 */
	public static final String PREF_CONNECTION_TYPE = "pref_connection_type";

	/**
	 * This is the default mavlink connection type
	 * 
	 * @since 1.2.0
	 */
	public static final String DEFAULT_CONNECTION_TYPE = ConnectionType.USB
			.name();

	/**
	 * Preference screen grouping the ui related preferences.
	 */
	public static final String PREF_UI_SCREEN = "pref_ui";

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
	private Constants() {
	}
}
