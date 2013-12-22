/** result in a mavlink mission ack
*/
package com.MAVLink.Messages.enums;

public class MAV_MISSION_RESULT {
	public static final int MAV_MISSION_ACCEPTED = 0; /* mission accepted OK | */
	public static final int MAV_MISSION_ERROR = 1; /* generic error / not accepting mission commands at all right now | */
	public static final int MAV_MISSION_UNSUPPORTED_FRAME = 2; /* coordinate frame is not supported | */
	public static final int MAV_MISSION_UNSUPPORTED = 3; /* command is not supported | */
	public static final int MAV_MISSION_NO_SPACE = 4; /* mission item exceeds storage space | */
	public static final int MAV_MISSION_INVALID = 5; /* one of the parameters has an invalid value | */
	public static final int MAV_MISSION_INVALID_PARAM1 = 6; /* param1 has an invalid value | */
	public static final int MAV_MISSION_INVALID_PARAM2 = 7; /* param2 has an invalid value | */
	public static final int MAV_MISSION_INVALID_PARAM3 = 8; /* param3 has an invalid value | */
	public static final int MAV_MISSION_INVALID_PARAM4 = 9; /* param4 has an invalid value | */
	public static final int MAV_MISSION_INVALID_PARAM5_X = 10; /* x/param5 has an invalid value | */
	public static final int MAV_MISSION_INVALID_PARAM6_Y = 11; /* y/param6 has an invalid value | */
	public static final int MAV_MISSION_INVALID_PARAM7 = 12; /* param7 has an invalid value | */
	public static final int MAV_MISSION_INVALID_SEQUENCE = 13; /* received waypoint out of sequence | */
	public static final int MAV_MISSION_DENIED = 14; /* not accepting any mission commands from this communication partner | */
	public static final int MAV_MISSION_RESULT_ENUM_END = 15; /*  | */
}
