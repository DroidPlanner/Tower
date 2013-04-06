/** Micro air vehicle / autopilot classes. This identifies the individual model.
*/
package com.MAVLink.Messages.enums;

public class MAV_AUTOPILOT {
	public static final int MAV_AUTOPILOT_GENERIC = 0; /* Generic autopilot, full support for everything | */
	public static final int MAV_AUTOPILOT_PIXHAWK = 1; /* PIXHAWK autopilot, http://pixhawk.ethz.ch | */
	public static final int MAV_AUTOPILOT_SLUGS = 2; /* SLUGS autopilot, http://slugsuav.soe.ucsc.edu | */
	public static final int MAV_AUTOPILOT_ARDUPILOTMEGA = 3; /* ArduPilotMega / ArduCopter, http://diydrones.com | */
	public static final int MAV_AUTOPILOT_OPENPILOT = 4; /* OpenPilot, http://openpilot.org | */
	public static final int MAV_AUTOPILOT_GENERIC_WAYPOINTS_ONLY = 5; /* Generic autopilot only supporting simple waypoints | */
	public static final int MAV_AUTOPILOT_GENERIC_WAYPOINTS_AND_SIMPLE_NAVIGATION_ONLY = 6; /* Generic autopilot supporting waypoints and other simple navigation commands | */
	public static final int MAV_AUTOPILOT_GENERIC_MISSION_FULL = 7; /* Generic autopilot supporting the full mission command set | */
	public static final int MAV_AUTOPILOT_INVALID = 8; /* No valid autopilot, e.g. a GCS or other MAVLink component | */
	public static final int MAV_AUTOPILOT_PPZ = 9; /* PPZ UAV - http://nongnu.org/paparazzi | */
	public static final int MAV_AUTOPILOT_UDB = 10; /* UAV Dev Board | */
	public static final int MAV_AUTOPILOT_FP = 11; /* FlexiPilot | */
	public static final int MAV_AUTOPILOT_PX4 = 12; /* PX4 Autopilot - http://pixhawk.ethz.ch/px4/ | */
	public static final int MAV_AUTOPILOT_ENUM_END = 13; /*  | */
}
