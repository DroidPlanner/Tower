package com.droidplanner.MAVLink;

import com.MAVLink.waypoint;
import com.MAVLink.Messages.ardupilotmega.msg_mission_ack;
import com.MAVLink.Messages.ardupilotmega.msg_mission_count;
import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.ardupilotmega.msg_mission_request;
import com.MAVLink.Messages.ardupilotmega.msg_mission_request_list;
import com.MAVLink.Messages.ardupilotmega.msg_mission_set_current;
import com.MAVLink.Messages.enums.MAV_FRAME;
import com.MAVLink.Messages.enums.MAV_MISSION_RESULT;
import com.droidplanner.service.MAVLinkClient;

public class MavLinkWaypoint {

	public static void sendAck(MAVLinkClient MavClient) {
		msg_mission_ack msg = new msg_mission_ack();
		msg.target_system = 1;
		msg.target_component = 1;
		msg.type = MAV_MISSION_RESULT.MAV_MISSION_ACCEPTED; 
		MavClient.sendMavPacket(msg.pack());

	}

	public static void requestWayPoint(MAVLinkClient MavClient, int index) {
		msg_mission_request msg = new msg_mission_request();
		msg.target_system = 1;
		msg.target_component = 1;
		msg.seq = (short) index;
		MavClient.sendMavPacket(msg.pack());
	}

	public static void requestWaypointsList(MAVLinkClient MavClient) {
		msg_mission_request_list msg = new msg_mission_request_list();
		msg.target_system = 1;
		msg.target_component = 1;
		MavClient.sendMavPacket(msg.pack());
	}

	public static void sendWaypoint(MAVLinkClient MavClient, int index,
			waypoint waypoint) {
		msg_mission_item msg = new msg_mission_item();
		msg.seq = (short) index;
		msg.current = (byte) ((index == 0) ? 1 : 0); // TODO use correct
														// parameter for HOME
		msg.frame = MAV_FRAME.MAV_FRAME_GLOBAL; 
		msg.command = 16; // TODO use correct parameter
		msg.param1 = 0; // TODO use correct parameter
		msg.param2 = 0; // TODO use correct parameter
		msg.param3 = 0; // TODO use correct parameter
		msg.param4 = 0; // TODO use correct parameter
		msg.x = (float) waypoint.coord.latitude;
		msg.y = (float) waypoint.coord.longitude;
		msg.z = waypoint.Height.floatValue();
		msg.autocontinue = 1; // TODO use correct parameter
		msg.target_system = 1;
		msg.target_component = 1;
		MavClient.sendMavPacket(msg.pack());
	}

	public static void sendWaypointCount(MAVLinkClient MavClient, int count) {
		msg_mission_count msg = new msg_mission_count();
		msg.target_system = 1;
		msg.target_component = 1;
		msg.count = (short) count;
		MavClient.sendMavPacket(msg.pack());
	}

	public static void sendSetCurrentWaypoint(MAVLinkClient MavClient, short i) {
		msg_mission_set_current msg = new msg_mission_set_current();
		msg.target_system = 1;
		msg.target_component = 1;
		msg.seq = i;
		MavClient.sendMavPacket(msg.pack());
	}

}
