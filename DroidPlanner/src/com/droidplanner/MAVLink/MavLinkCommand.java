package com.droidplanner.MAVLink;

import com.MAVLink.Messages.ardupilotmega.msg_command_ack;
import com.MAVLink.Messages.ardupilotmega.msg_command_long;
import com.MAVLink.Messages.enums.MAV_CMD;
import com.MAVLink.Messages.enums.MAV_GOTO;
import com.droidplanner.drone.Drone;

public class MavLinkCommand {

	public static void sendLaunchMessage(Drone drone) {
		msg_command_long msg = new msg_command_long();
		msg.target_system = 1;
		msg.target_component = 1;

		msg.command = (short) MAV_CMD.MAV_CMD_NAV_TAKEOFF;
		msg.param1 = 0;
		msg.param2 = 0;
		msg.param3 = 0;
		msg.param4 = 0;
		msg.param5 = 0; // Do we need coordinates?  Ar Drone seems to start mission
		msg.param6 = 0;
		msg.param7 = 0;
		msg.confirmation = 0;
		drone.MavClient.sendMavPacket(msg.pack());
	}

	public static void sendLandMessage(Drone drone) {
		msg_command_long msg = new msg_command_long();
		msg.target_system = 1;
		msg.target_component = 1;

		msg.command = (short) MAV_CMD.MAV_CMD_NAV_LAND;
		msg.param1 = 0;
		msg.param2 = 0;
		msg.param3 = 0;
		msg.param4 = 0;
		msg.param5 = 0; // Do we need coordinates?  AR Drone seems to land at current position
		msg.param6 = 0;
		msg.param7 = 0;
		msg.confirmation = 0;
		drone.MavClient.sendMavPacket(msg.pack());
	}

	public static void sendReturnToLaunchMessage(Drone drone) {
		msg_command_long msg = new msg_command_long();
		msg.target_system = 1;
		msg.target_component = 1;

		msg.command = (short) MAV_CMD.MAV_CMD_NAV_RETURN_TO_LAUNCH;
		msg.param1 = 0;
		msg.param2 = 0;
		msg.param3 = 0;
		msg.param4 = 0;
		msg.param5 = 0;
		msg.param6 = 0;
		msg.param7 = 0;
		msg.confirmation = 0;
		drone.MavClient.sendMavPacket(msg.pack());
	}

	public static void sendHoldMessage(Drone drone) {
		msg_command_long msg = new msg_command_long();
		msg.target_system = 1;
		msg.target_component = 1;

		msg.command = (short) MAV_CMD.MAV_CMD_OVERRIDE_GOTO;
		msg.param1 = MAV_GOTO.MAV_GOTO_DO_HOLD;
		msg.param2 = MAV_GOTO.MAV_GOTO_HOLD_AT_CURRENT_POSITION;
		msg.param3 = 0;
		msg.param4 = 0;
		msg.param5 = 0;
		msg.param6 = 0;
		msg.param7 = 0;
		msg.confirmation = 0;
		drone.MavClient.sendMavPacket(msg.pack());
	}

	public static void sendContinueMessage(Drone drone) {
		msg_command_long msg = new msg_command_long();
		msg.target_system = 1;
		msg.target_component = 1;
		
		msg.command = (short) MAV_CMD.MAV_CMD_OVERRIDE_GOTO;
		msg.param1 = MAV_GOTO.MAV_GOTO_DO_CONTINUE;
		msg.param2 = 0;
		msg.param3 = 0;
		msg.param4 = 0;
		msg.param5 = 0;
		msg.param6 = 0;
		msg.param7 = 0;
		msg.confirmation = 0;
		drone.MavClient.sendMavPacket(msg.pack());
	}

}
