/** These values encode the bit positions of the decode position. These values can be used to read the value of a flag bit by combining the base_mode variable with AND with the flag position value. The result will be either 0 or 1, depending on if the flag is set or not.
*/
package com.MAVLink.Messages.enums;

public class MAV_MODE_FLAG_DECODE_POSITION {
	public static final int MAV_MODE_FLAG_DECODE_POSITION_CUSTOM_MODE = 1; /* Eighth bit: 00000001 | */
	public static final int MAV_MODE_FLAG_DECODE_POSITION_TEST = 2; /* Seventh bit: 00000010 | */
	public static final int MAV_MODE_FLAG_DECODE_POSITION_AUTO = 4; /* Sixt bit:   00000100 | */
	public static final int MAV_MODE_FLAG_DECODE_POSITION_GUIDED = 8; /* Fifth bit:  00001000 | */
	public static final int MAV_MODE_FLAG_DECODE_POSITION_STABILIZE = 16; /* Fourth bit: 00010000 | */
	public static final int MAV_MODE_FLAG_DECODE_POSITION_HIL = 32; /* Third bit:  00100000 | */
	public static final int MAV_MODE_FLAG_DECODE_POSITION_MANUAL = 64; /* Second bit: 01000000 | */
	public static final int MAV_MODE_FLAG_DECODE_POSITION_SAFETY = 128; /* First bit:  10000000 | */
	public static final int MAV_MODE_FLAG_DECODE_POSITION_ENUM_END = 129; /*  | */
}
