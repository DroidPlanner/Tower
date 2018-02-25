package org.droidplanner.services.android.impl.core.MAVLink.connection;

/**
 * List the supported mavlink connection types.
 */
public class MavLinkConnectionTypes {

	/**
	 * Bluetooth mavlink connection.
	 */
	public static final int MAVLINK_CONNECTION_BLUETOOTH = 3;

	/**
	 * USP mavlink connection.
	 */
	public static final int MAVLINK_CONNECTION_USB = 0;

	/**
	 * UDP mavlink connection.
	 */
	public static final int MAVLINK_CONNECTION_UDP = 1;

	/**
	 * TCP mavlink connection.
	 */
	public static final int MAVLINK_CONNECTION_TCP = 2;

	// Not instantiable
	private MavLinkConnectionTypes() {
	}
}
