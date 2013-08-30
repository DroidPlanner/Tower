package com.MAVLink.Messages;

import java.util.ArrayList;

import com.MAVLink.Messages.enums.MAV_CMD;

public enum ApmCommands {

	CMD_NAV_WAYPOINT ("Waypoint",MAV_CMD.MAV_CMD_NAV_WAYPOINT), /* Navigate to MISSION. |Hold time in decimal seconds. (ignored by fixed wing, time to stay at MISSION for rotary wing)| Acceptance radius in meters (if the sphere with this radius is hit, the MISSION counts as reached)| 0 to pass through the WP, if > 0 radius in meters to pass by WP. Positive value for clockwise orbit, negative value for counter-clockwise orbit. Allows trajectory control.| Desired yaw angle at MISSION (rotary wing)| Latitude| Longitude| Altitude|  */
	CMD_NAV_LOITER_UNLIM("Loiter",MAV_CMD.MAV_CMD_NAV_LOITER_UNLIM ), /* Loiter around this MISSION an unlimited amount of time |Empty| Empty| Radius around MISSION, in meters. If positive loiter clockwise, else counter-clockwise| Desired yaw angle.| Latitude| Longitude| Altitude|  */
	CMD_NAV_LOITER_TURNS("LoiterN",MAV_CMD.MAV_CMD_NAV_LOITER_TURNS ), /* Loiter around this MISSION for X turns |Turns| Empty| Radius around MISSION, in meters. If positive loiter clockwise, else counter-clockwise| Desired yaw angle.| Latitude| Longitude| Altitude|  */
	CMD_NAV_LOITER_TIME ("LoiterT",MAV_CMD.MAV_CMD_NAV_LOITER_TIME ), /* Loiter around this MISSION for X seconds |Seconds (decimal)| Empty| Radius around MISSION, in meters. If positive loiter clockwise, else counter-clockwise| Desired yaw angle.| Latitude| Longitude| Altitude|  */
	CMD_NAV_RETURN_TO_LAUNCH("RTL",MAV_CMD.MAV_CMD_NAV_RETURN_TO_LAUNCH), /* Return to launch location |Empty| Empty| Empty| Empty| Empty| Empty| Empty|  */
	CMD_NAV_LAND("Land",MAV_CMD.MAV_CMD_NAV_LAND), /* Land at location |Empty| Empty| Empty| Desired yaw angle.| Latitude| Longitude| Altitude|  */
	CMD_NAV_TAKEOFF("Takeoff",MAV_CMD.MAV_CMD_NAV_TAKEOFF), /* Takeoff from ground / hand |Minimum pitch (if airspeed sensor present), desired pitch without sensor| Empty| Empty| Yaw angle (if magnetometer present), ignored without magnetometer| Latitude| Longitude| Altitude|  */
	CMD_NAV_ROI("ROI",MAV_CMD.MAV_CMD_NAV_ROI), /* Sets the region of interest (ROI) for a sensor set or the vehicle itself. This can then be used by the vehicles control system to control the vehicle attitude and the attitude of various sensors such as cameras. |Region of intereset mode. (see MAV_ROI enum)| MISSION index/ target ID. (see MAV_ROI enum)| ROI index (allows a vehicle to manage multiple ROI's)| Empty| x the location of the fixed ROI (see MAV_FRAME)| y| z|  */
	CMD_NAV_PATHPLANNING("Path",MAV_CMD.MAV_CMD_NAV_PATHPLANNING), /* Control autonomous path planning on the MAV. |0: Disable local obstacle avoidance / local path planning (without resetting map), 1: Enable local path planning, 2: Enable and reset local path planning| 0: Disable full path planning (without resetting map), 1: Enable, 2: Enable and reset map/occupancy grid, 3: Enable and reset planned route, but not occupancy grid| Empty| Yaw angle at goal, in compass degrees, [0..360]| Latitude/X of goal| Longitude/Y of goal| Altitude/Z of goal|  */
	CMD_DO_JUMP("Do Jump",MAV_CMD.MAV_CMD_DO_JUMP), /* Jump to the desired command in the mission list.  Repeat this action only the specified number of times |Sequence number| Repeat count| Empty| Empty| Empty| Empty| Empty|  */
	CMD_CONDITION_YAW("Yaw to",MAV_CMD.MAV_CMD_CONDITION_YAW); /* Yaw to heading while executing next waypoint.  |  Target angle: [0-360], 0 is north. |    speed during yaw change:[deg per second] |      direction: negative: counter clockwise, positive: clockwise [-1,1] | relative offset or absolute angle: [ 1,0] | Empty| Empty| Empty|  */
    private final String name;
	private final int type;
    
	ApmCommands(String name, int type){
		this.name = name;
		this.type = type;
	}
	
	public String getName() {
		return name;
	}
	
	public int getType() {
		return type;
	}
	
	public static ApmCommands getCmd(int type) {
		for (ApmCommands mode : ApmCommands.values()) {
			if (type == mode.getType()) {
				return mode;
			}
		}
		return null;
	}	
	
	public static ApmCommands getCmd(String str) {
		for (ApmCommands mode : ApmCommands.values()) {
			if (str.equals(mode.getName())) {
				return mode;
			}
		}
		return null;
	}	
	
	public static ArrayList<String> getNameList() {
		ArrayList<String> list = new ArrayList<String>();
		
		for (ApmCommands mode : ApmCommands.values()) {
				list.add(mode.getName());
		}
		return list;
	}
		
}
