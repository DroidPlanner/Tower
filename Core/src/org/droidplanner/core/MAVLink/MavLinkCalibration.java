package org.droidplanner.core.MAVLink;

import org.droidplanner.core.drone.Drone;

import com.MAVLink.Messages.ardupilotmega.msg_command_ack;
import com.MAVLink.Messages.ardupilotmega.msg_command_long;
import com.MAVLink.Messages.enums.MAV_CMD;
import com.MAVLink.Messages.enums.MAV_CMD_ACK;

public class MavLinkCalibration {

	public static void sendCalibrationAckMessage(int count, Drone drone) {
		msg_command_ack msg = new msg_command_ack();
		msg.command = (short) count;
		msg.result = MAV_CMD_ACK.MAV_CMD_ACK_OK;
		drone.MavClient.sendMavPacket(msg.pack());
	}

	public static void sendStartCalibrationMessage(Drone drone) {
		msg_command_long msg = new msg_command_long();
		msg.target_system = 1;
		msg.target_component = 1;

		msg.command = MAV_CMD.MAV_CMD_PREFLIGHT_CALIBRATION;
		msg.param1 = 0;
		msg.param2 = 0;
		msg.param3 = 0;
		msg.param4 = 0;
		msg.param5 = 1;
		msg.param6 = 0;
		msg.param7 = 0;
		msg.confirmation = 0;
		drone.MavClient.sendMavPacket(msg.pack());
	}

}
