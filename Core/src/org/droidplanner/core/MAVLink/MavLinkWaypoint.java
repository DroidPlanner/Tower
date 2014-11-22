package org.droidplanner.core.MAVLink;

import org.droidplanner.core.model.Drone;

import com.MAVLink.common.msg_mission_ack;
import com.MAVLink.common.msg_mission_count;
import com.MAVLink.common.msg_mission_request;
import com.MAVLink.common.msg_mission_request_list;
import com.MAVLink.common.msg_mission_set_current;
import com.MAVLink.enums.MAV_MISSION_RESULT;

public class MavLinkWaypoint {

	public static void sendAck(Drone drone) {
		msg_mission_ack msg = new msg_mission_ack();
		msg.target_system = drone.getSysid();
		msg.target_component = drone.getCompid();
		msg.type = MAV_MISSION_RESULT.MAV_MISSION_ACCEPTED;
		drone.getMavClient().sendMavPacket(msg.pack());

	}

	public static void requestWayPoint(Drone drone, int index) {
		msg_mission_request msg = new msg_mission_request();
		msg.target_system = drone.getSysid();
		msg.target_component = drone.getCompid();
		msg.seq = (short) index;
		drone.getMavClient().sendMavPacket(msg.pack());
	}

	public static void requestWaypointsList(Drone drone) {
		msg_mission_request_list msg = new msg_mission_request_list();
		msg.target_system = drone.getSysid();
		msg.target_component = drone.getCompid();
		drone.getMavClient().sendMavPacket(msg.pack());
	}

	public static void sendWaypointCount(Drone drone, int count) {
		msg_mission_count msg = new msg_mission_count();
		msg.target_system = drone.getSysid();
		msg.target_component = drone.getCompid();
		msg.count = (short) count;
		drone.getMavClient().sendMavPacket(msg.pack());
	}

	public static void sendSetCurrentWaypoint(Drone drone, short i) {
		msg_mission_set_current msg = new msg_mission_set_current();
		msg.target_system = drone.getSysid();
		msg.target_component = drone.getCompid();
		msg.seq = i;
		drone.getMavClient().sendMavPacket(msg.pack());
	}

}
