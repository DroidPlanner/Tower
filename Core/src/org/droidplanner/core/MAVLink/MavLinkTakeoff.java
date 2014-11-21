package org.droidplanner.core.MAVLink;

import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.model.Drone;

import com.MAVLink.common.msg_command_long;
import com.MAVLink.enums.MAV_CMD;

public class MavLinkTakeoff {
	public static void sendTakeoff(Drone drone, Altitude alt) {
		msg_command_long msg = new msg_command_long();
		msg.target_system = drone.getSysid();
		msg.target_component = drone.getCompid();
		msg.command = MAV_CMD.MAV_CMD_NAV_TAKEOFF;

		msg.param7 = (float) alt.valueInMeters();

		drone.getMavClient().sendMavPacket(msg.pack());
	}
}
