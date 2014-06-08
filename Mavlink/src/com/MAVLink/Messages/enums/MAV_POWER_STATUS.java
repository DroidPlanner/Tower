/** Power supply status flags (bitmask)
*/
package com.MAVLink.Messages.enums;

public class MAV_POWER_STATUS {
	public static final int MAV_POWER_STATUS_BRICK_VALID = 1; /* main brick power supply valid | */
	public static final int MAV_POWER_STATUS_SERVO_VALID = 2; /* main servo power supply valid for FMU | */
	public static final int MAV_POWER_STATUS_USB_CONNECTED = 4; /* USB power is connected | */
	public static final int MAV_POWER_STATUS_PERIPH_OVERCURRENT = 8; /* peripheral supply is in over-current state | */
	public static final int MAV_POWER_STATUS_PERIPH_HIPOWER_OVERCURRENT = 16; /* hi-power peripheral supply is in over-current state | */
	public static final int MAV_POWER_STATUS_CHANGED = 32; /* Power status has changed since boot | */
	public static final int MAV_POWER_STATUS_ENUM_END = 33; /*  | */
}
