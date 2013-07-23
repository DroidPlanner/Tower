package com.droidplanner.MAVLink;

import com.MAVLink.Messages.ApmModes;
import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.ardupilotmega.msg_set_mode;
import com.MAVLink.Messages.enums.MAV_CMD;
import com.MAVLink.Messages.enums.MAV_FRAME;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.service.MAVLinkClient;

public class MavLinkModes {
	public static void setGuidedMode(MAVLinkClient MAVClient, waypoint wp) {
		msg_mission_item msg = new msg_mission_item();
		msg.seq = 0;
		msg.current = 2; // TODO use guided mode enum
		msg.frame = MAV_FRAME.MAV_FRAME_GLOBAL;
		msg.command = MAV_CMD.MAV_CMD_NAV_WAYPOINT; //
		msg.param1 = 0; // TODO use correct parameter
		msg.param2 = 0; // TODO use correct parameter
		msg.param3 = 0; // TODO use correct parameter
		msg.param4 = 0; // TODO use correct parameter
		msg.x = (float) wp.getCoord().latitude;
		msg.y = (float) wp.getCoord().longitude;
		msg.z = wp.getHeight().floatValue();
		msg.autocontinue = 1; // TODO use correct parameter
		msg.target_system = 1;
		msg.target_component = 1;
		MAVClient.sendMavPacket(msg.pack());
	}

	public static void changeFlightMode(MAVLinkClient MAVClient, ApmModes mode) {
		msg_set_mode msg = new msg_set_mode();
		msg.target_system = 1;
		msg.base_mode = 1; // TODO use meaningful constant
		msg.custom_mode = mode.getNumber();
		MAVClient.sendMavPacket(msg.pack());
	}
}
