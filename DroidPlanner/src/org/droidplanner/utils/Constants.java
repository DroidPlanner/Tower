package org.droidplanner.utils;

import static org.droidplanner.utils.Utils.ConnectionType;

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
     * The possible values are members of the {@link org.droidplanner.utils.Utils.ConnectionType}
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
    public static final String DEFAULT_CONNECTION_TYPE = ConnectionType.USB.name();

    /**
     * Preference screen grouping the ui related preferences.
     */
    public static final String PREF_UI_SCREEN = "pref_ui";

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private Constants() {
    }
}
