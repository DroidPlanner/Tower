package org.droidplanner.core.MAVLink.connection;

/**
 * List the supported mavlink connection types.
 */
public class MavLinkConnectionTypes {

	/**
	 * Bluetooth mavlink connection.
	 */
	public static final int MAVLINK_CONNECTION_BLUETOOTH = 0;

	/**
	 * USP mavlink connection.
	 */
	public static final int MAVLINK_CONNECTION_USB = 1;

	/**
	 * UDP mavlink connection.
	 */
	public static final int MAVLINK_CONNECTION_UDP = 2;

	/**
	 * TCP mavlink connection.
	 */
	public static final int MAVLINK_CONNECTION_TCP = 3;

	// Not instantiable
	private MavLinkConnectionTypes() {
	}
}
