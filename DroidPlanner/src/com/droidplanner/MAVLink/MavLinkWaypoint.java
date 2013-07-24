package com.droidplanner.MAVLink;

import com.MAVLink.Messages.ardupilotmega.msg_mission_ack;
import com.MAVLink.Messages.ardupilotmega.msg_mission_count;
import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.ardupilotmega.msg_mission_request;
import com.MAVLink.Messages.ardupilotmega.msg_mission_request_list;
import com.MAVLink.Messages.ardupilotmega.msg_mission_set_current;
import com.MAVLink.Messages.enums.MAV_FRAME;
import com.MAVLink.Messages.enums.MAV_MISSION_RESULT;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.variables.waypoint;

public class MavLinkWaypoint {

	public static void sendAck(Drone drone) {
		msg_mission_ack msg = new msg_mission_ack();
		msg.target_system = 1;
		msg.target_component = 1;
		msg.type = MAV_MISSION_RESULT.MAV_MISSION_ACCEPTED;
		drone.MavClient.sendMavPacket(msg.pack());

	}

	public static void requestWayPoint(Drone drone, int index) {
		msg_mission_request msg = new msg_mission_request();
		msg.target_system = 1;
		msg.target_component = 1;
		msg.seq = (short) index;
		drone.MavClient.sendMavPacket(msg.pack());
	}

	public static void requestWaypointsList(Drone drone) {
		msg_mission_request_list msg = new msg_mission_request_list();
		msg.target_system = 1;
		msg.target_component = 1;
		drone.MavClient.sendMavPacket(msg.pack());
	}

	public static void sendWaypoint(Drone drone, int index,
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
		msg.x = (float) waypoint.getCoord().latitude;
		msg.y = (float) waypoint.getCoord().longitude;
		msg.z = waypoint.getHeight().floatValue();
		msg.autocontinue = 1; // TODO use correct parameter
		msg.target_system = 1;
		msg.target_component = 1;
		drone.MavClient.sendMavPacket(msg.pack());
	}

	public static void sendWaypointCount(Drone drone, int count) {
		msg_mission_count msg = new msg_mission_count();
		msg.target_system = 1;
		msg.target_component = 1;
		msg.count = (short) count;
		drone.MavClient.sendMavPacket(msg.pack());
	}

	public static void sendSetCurrentWaypoint(Drone drone, short i) {
		msg_mission_set_current msg = new msg_mission_set_current();
		msg.target_system = 1;
		msg.target_component = 1;
		msg.seq = i;
		drone.MavClient.sendMavPacket(msg.pack());
	}

}
