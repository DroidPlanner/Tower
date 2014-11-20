package org.droidplanner.core.MAVLink;

import org.droidplanner.core.model.Drone;

import com.MAVLink.Messages.ApmModes;
import com.MAVLink.common.msg_mission_item;
import com.MAVLink.common.msg_set_mode;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_FRAME;

public class MavLinkModes {
	public static void setGuidedMode(Drone drone, double latitude, double longitude, double d) {
		msg_mission_item msg = new msg_mission_item();
		msg.seq = 0;
		msg.current = 2; // TODO use guided mode enum
		msg.frame = MAV_FRAME.MAV_FRAME_GLOBAL;
		msg.command = MAV_CMD.MAV_CMD_NAV_WAYPOINT; //
		msg.param1 = 0; // TODO use correct parameter
		msg.param2 = 0; // TODO use correct parameter
		msg.param3 = 0; // TODO use correct parameter
		msg.param4 = 0; // TODO use correct parameter
		msg.x = (float) latitude;
		msg.y = (float) longitude;
		msg.z = (float) d;
		msg.autocontinue = 1; // TODO use correct parameter
		msg.target_system = 1;
		msg.target_component = 1;
		drone.getMavClient().sendMavPacket(msg.pack());
	}

	public static void sendGuidedVelocity(Drone drone, double xVel, double yVel, double zVel) {
		msg_mission_item msg = new msg_mission_item();
		msg.seq = 0;
		msg.current = 2; // TODO use guided mode enum
		msg.frame = MAV_FRAME.MAV_FRAME_GLOBAL;
		msg.command =  91; // MAV_CMD_NAV_VELOCITY
		msg.param1 = 0; // TODO use correct parameter
		msg.param2 = 0; // TODO use correct parameter
		msg.param3 = 0; // TODO use correct parameter
		msg.param4 = 0; // TODO use correct parameter
		msg.x = (float) (xVel );
		msg.y = (float) (yVel);
		msg.z = (float) (zVel);
		msg.autocontinue = 1; // TODO use correct parameter
		msg.target_system = 1;
		msg.target_component = 1;
		drone.getMavClient().sendMavPacket(msg.pack());
	}

	public static void changeFlightMode(Drone drone, ApmModes mode) {
		msg_set_mode msg = new msg_set_mode();
		msg.target_system = 1;
		msg.base_mode = 1; // TODO use meaningful constant
		msg.custom_mode = mode.getNumber();
		drone.getMavClient().sendMavPacket(msg.pack());
	}
}
