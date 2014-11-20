package com.MAVLink.Messages;

import java.util.ArrayList;

import com.MAVLink.enums.MAV_CMD;

public enum ApmCommands {	

	CMD_NAV_WAYPOINT ("Waypoint",MAV_CMD.MAV_CMD_NAV_WAYPOINT,CommandType.NAVIGATION), /* Navigate to MISSION. |Hold time in decimal seconds. (ignored by fixed wing, time to stay at MISSION for rotary wing)| Acceptance radius in meters (if the sphere with this radius is hit, the MISSION counts as reached)| 0 to pass through the WP, if > 0 radius in meters to pass by WP. Positive value for clockwise orbit, negative value for counter-clockwise orbit. Allows trajectory control.| Desired yaw angle at MISSION (rotary wing)| Latitude| Longitude| Altitude|  */
	CMD_NAV_LOITER_UNLIM("Loiter",MAV_CMD.MAV_CMD_NAV_LOITER_UNLIM ,CommandType.NAVIGATION), /* Loiter around this MISSION an unlimited amount of time |Empty| Empty| Radius around MISSION, in meters. If positive loiter clockwise, else counter-clockwise| Desired yaw angle.| Latitude| Longitude| Altitude|  */
	CMD_NAV_LOITER_TURNS("LoiterN",MAV_CMD.MAV_CMD_NAV_LOITER_TURNS ,CommandType.NAVIGATION), /* Loiter around this MISSION for X turns |Turns| Empty| Radius around MISSION, in meters. If positive loiter clockwise, else counter-clockwise| Desired yaw angle.| Latitude| Longitude| Altitude|  */
	CMD_NAV_LOITER_TIME ("LoiterT",MAV_CMD.MAV_CMD_NAV_LOITER_TIME ,CommandType.NAVIGATION), /* Loiter around this MISSION for X seconds |Seconds (decimal)| Empty| Radius around MISSION, in meters. If positive loiter clockwise, else counter-clockwise| Desired yaw angle.| Latitude| Longitude| Altitude|  */
	CMD_NAV_RETURN_TO_LAUNCH("RTL",MAV_CMD.MAV_CMD_NAV_RETURN_TO_LAUNCH,CommandType.COMMAND), /* Return to launch location |Empty| Empty| Empty| Empty| Empty| Empty| Empty|  */
	CMD_NAV_LAND("Land",MAV_CMD.MAV_CMD_NAV_LAND,CommandType.NAVIGATION), /* Land at location |Empty| Empty| Empty| Desired yaw angle.| Latitude| Longitude| Altitude|  */
	CMD_NAV_TAKEOFF("Takeoff",MAV_CMD.MAV_CMD_NAV_TAKEOFF,CommandType.NAVIGATION), /* Takeoff from ground / hand |Minimum pitch (if airspeed sensor present), desired pitch without sensor| Empty| Empty| Yaw angle (if magnetometer present), ignored without magnetometer| Latitude| Longitude| Altitude|  */
	CMD_NAV_ROI("ROI",MAV_CMD.MAV_CMD_NAV_ROI,CommandType.COMMAND_WITH_TARGET), /* Sets the region of interest (ROI) for a sensor set or the vehicle itself. This can then be used by the vehicles control system to control the vehicle attitude and the attitude of various sensors such as cameras. |Region of intereset mode. (see MAV_ROI enum)| MISSION index/ target ID. (see MAV_ROI enum)| ROI index (allows a vehicle to manage multiple ROI's)| Empty| x the location of the fixed ROI (see MAV_FRAME)| y| z|  */
	CMD_NAV_PATHPLANNING("Path",MAV_CMD.MAV_CMD_NAV_PATHPLANNING,CommandType.COMMAND), /* Control autonomous path planning on the MAV. |0: Disable local obstacle avoidance / local path planning (without resetting map), 1: Enable local path planning, 2: Enable and reset local path planning| 0: Disable full path planning (without resetting map), 1: Enable, 2: Enable and reset map/occupancy grid, 3: Enable and reset planned route, but not occupancy grid| Empty| Yaw angle at goal, in compass degrees, [0..360]| Latitude/X of goal| Longitude/Y of goal| Altitude/Z of goal|  */
	CMD_DO_JUMP("Do Jump",MAV_CMD.MAV_CMD_DO_JUMP,CommandType.COMMAND), /* Jump to the desired command in the mission list.  Repeat this action only the specified number of times |Sequence number| Repeat count| Empty| Empty| Empty| Empty| Empty|  */
	CMD_DO_SET_HOME("Set Home",MAV_CMD.MAV_CMD_DO_SET_HOME,CommandType.COMMAND_WITH_TARGET), /*	Changes the home location either to the current location or a specified location.| Use current (1=use current location, 0=use specified location) | Empty	| Empty	| Empty | Latitude | Longitude | Altitude*/	
	CMD_DO_CHANGE_SPEED("Set Speed",MAV_CMD.MAV_CMD_DO_CHANGE_SPEED,CommandType.COMMAND), /*	Change speed and/or throttle set points.| Speed type (0=Airspeed, 1=Ground Speed) | Speed (m/s, -1 indicates no change)	| Throttle ( Percent, -1 indicates no change) | Empty | Empty | Empty | Empty*/
	CMD_CONDITION_CHANGE_ALT("Set Alt",MAV_CMD.MAV_CMD_CONDITION_CHANGE_ALT,CommandType.NAVIGATION),/*Ascend/descend at rate. Delay mission state machine until desired altitude reached.| Descent / Ascend rate (m/s) |	Empty |	Empty |	Empty | Empty | Empty | Finish Altitude | */
	CMD_CONDITION_DISTANCE("Set Distance",MAV_CMD.MAV_CMD_CONDITION_DISTANCE,CommandType.COMMAND),	/*Delay mission state machine until within desired distance of next NAV point.| Distance (meters) | Empty | Empty | Empty | Empty | Empty | Empty*/
	CMD_CONDITION_YAW("Yaw to",MAV_CMD.MAV_CMD_CONDITION_YAW,CommandType.COMMAND),  /* Yaw to heading while executing next waypoint.  |  Target angle: [0-360], 0 is north. |    speed during yaw change:[deg per second] |      direction: negative: counter clockwise, positive: clockwise [-1,1] | relative offset or absolute angle: [ 1,0] | Empty| Empty| Empty|  */
//	CMD_DO_SET_RELAY("Set Relay",MAV_CMD.MAV_CMD_DO_SET_RELAY,CommandType.COMMAND),/*	Set a relay to a condition.| Relay number	| Setting (1=on, 0=off, others possible depending on system hardware) | Empty | Empty | Empty	| Empty | Empty*/
//	CMD_DO_REPEAT_RELAY("Repeat Relay",MAV_CMD.MAV_CMD_DO_REPEAT_RELAY,CommandType.COMMAND),/*	Cycle a relay on and off for a desired number of cyles with a desired period.	| Relay number	| Cycle count	| Cycle time (seconds, decimal)	| Empty	| Empty	| Empty	| Empty	*/
	; 

	private final String name;
	private final int arduPilotIntValue;
	private final CommandType commandType;
    
	ApmCommands(String name, int type, CommandType showOnMap){
		this.name = name;
		this.arduPilotIntValue = type;
		this.commandType = showOnMap; 
	}
	
	public String getName() {
		return name;
	}
	
	public int getType() {
		return arduPilotIntValue;
	}
	
	public boolean showOnMap(){
		switch (this.commandType) {
		case COMMAND:
			return false;
		default:
		case COMMAND_WITH_TARGET:
		case NAVIGATION:
			return true;
		}
	}
	
	public boolean isOnFligthPath(){
		switch (this.commandType) {
		default:
		case COMMAND:
		case COMMAND_WITH_TARGET:
			return false;
		case NAVIGATION:
			return true;
		}
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
		

	private enum CommandType{
		NAVIGATION,
		COMMAND,
		COMMAND_WITH_TARGET
	}
	
}
