/** 
*/
package com.MAVLink.Messages.enums;

public class MAV_FRAME {
	public static final int MAV_FRAME_GLOBAL = 0; /* Global coordinate frame, WGS84 coordinate system. First value / x: latitude, second value / y: longitude, third value / z: positive altitude over mean sea level (MSL) | */
	public static final int MAV_FRAME_LOCAL_NED = 1; /* Local coordinate frame, Z-up (x: north, y: east, z: down). | */
	public static final int MAV_FRAME_MISSION = 2; /* NOT a coordinate frame, indicates a mission command. | */
	public static final int MAV_FRAME_GLOBAL_RELATIVE_ALT = 3; /* Global coordinate frame, WGS84 coordinate system, relative altitude over ground with respect to the home position. First value / x: latitude, second value / y: longitude, third value / z: positive altitude with 0 being at the altitude of the home location. | */
	public static final int MAV_FRAME_LOCAL_ENU = 4; /* Local coordinate frame, Z-down (x: east, y: north, z: up) | */
	public static final int MAV_FRAME_ENUM_END = 5; /*  | */
}
