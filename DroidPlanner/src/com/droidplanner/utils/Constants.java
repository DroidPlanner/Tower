package com.droidplanner.utils;

import static com.droidplanner.glass.utils.GlassUtils.isGlassDevice;
import static com.droidplanner.utils.Utils.ConnectionType;

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
    private static final String PACKAGE_NAME = Constants.class.getPackage().getName();

    /*
    Preferences, and default values.
     */
    public static final String PREF_BLUETOOTH_DEVICE_ADDRESS = "pref_bluetooth_device_address";

    /**
     * This is the preference for the connection type.
     * The possible values are members of the {@link com.droidplanner.utils.Utils.ConnectionType}
     * enum.
     *
     * @since 1.2.0
     */
    public static final String PREF_CONNECTION_TYPE = "pref_connection_type";

    /**
     * This is the default mavlink connection type
     *
     * @since 1.2.0
     */
    public static final String DEFAULT_CONNECTION_TYPE = isGlassDevice()
            ? ConnectionType.BLUETOOTH.name()
            : ConnectionType.USB.name();

    /**
     * This preference controls the activation of the mavlink bluetooth relay server.
     * @since 1.2.0
     */
    public static final String PREF_BLUETOOTH_RELAY_SERVER_TOGGLE =
            "pref_bluetooth_relay_server_toggle";

    /**
     * By default, the mavlink bluetooth relay server is turned off.
     * @since 1.2.0
     */
    public static final boolean DEFAULT_BLUETOOTH_RELAY_SERVER_TOGGLE = false;

    /**
     * Preference screen grouping the ui related preferences.
     */
    public static final String PREF_UI_SCREEN = "pref_ui";

    /**
     * This preference controls the use of voice to control the user interface for Google Glass.
     */
    public static final String PREF_GLASS_VOICE_CONTROL = "pref_glass_voice_control";

    /**
     * By default, the use of voice to control the Google Glass user interface is disabled.
     */
    public static final boolean DEFAULT_GLASS_VOICE_CONTROL = false;

    /*
    Intent actions
     */
    private static final String PREFIX_ACTION = PACKAGE_NAME + ".action.";
    public static final String ACTION_BLUETOOTH_RELAY_SERVER = PREFIX_ACTION + "RELAY_SERVER";


    /*
    Bundle extras
     */
    private static final String PREFIX_EXTRA = PACKAGE_NAME + ".extra.";
    public static final String EXTRA_BLUETOOTH_RELAY_SERVER_ENABLED = PREFIX_EXTRA +
            "BLUETOOTH_RELAY_SERVER_ENABLED";

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private Constants() {
    }
}
