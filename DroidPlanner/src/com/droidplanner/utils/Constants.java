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
    public static final String PREF_MENU_DRAWER_LOCK = "pref_menu_drawer_lock";
    public static final boolean DEFAULT_MENU_DRAWER_LOCK = false;

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
    public static final String PREF_MAVLINK_BLUETOOTH_RELAY_SERVER_TOGGLE =
            "pref_mavlink_bluetooth_relay_server_toggle";

    /**
     * By default, the mavlink bluetooth relay server is turned off.
     * @since 1.2.0
     */
    public static final boolean DEFAULT_MAVLINK_BLUETOOTH_RELAY_SERVER_TOGGLE = false;


    /*
    Intent actions
     */
    private static final String PREFIX_ACTION = PACKAGE_NAME + ".action.";

    public static final String ACTION_MENU_DRAWER_LOCK_UPDATE = PREFIX_ACTION +
            "MENU_DRAWER_LOCK_UPDATE";
    public static final String ACTION_CONFIGURATION_TUNING = PREFIX_ACTION +
            "CONFIGURATION_TUNING";
    public static final String ACTION_CONFIGURATION_RC = PREFIX_ACTION + "CONFIGURATION_RC";
    public static final String ACTION_CONFIGURATION_PARAMETERS = PREFIX_ACTION +
            "CONFIGURATION_PARAMETERS";
    public static final String ACTION_CONFIGURATION_SETTINGS = PREFIX_ACTION +
            "CONFIGURATION_SETTINGS";


    /*
    Bundle extras
     */
    private static final String PREFIX_EXTRA = PACKAGE_NAME + ".extra.";
    public static final String EXTRA_MENU_DRAWER_LOCK = PREFIX_EXTRA + "MENU_DRAWER_LOCK";

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private Constants() {
    }
}
