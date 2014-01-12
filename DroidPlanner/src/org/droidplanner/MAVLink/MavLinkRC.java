package org.droidplanner.MAVLink;

import org.droidplanner.drone.Drone;

import com.MAVLink.Messages.ardupilotmega.msg_rc_channels_override;

public class MavLinkRC {
	public static void sendRcOverrideMsg(Drone drone, int[] rcOutputs) {
		msg_rc_channels_override msg = new msg_rc_channels_override();
		msg.chan1_raw = (short) rcOutputs[0];
		msg.chan2_raw = (short) rcOutputs[1];
		msg.chan3_raw = (short) rcOutputs[2];
		msg.chan4_raw = (short) rcOutputs[3];
		msg.chan5_raw = (short) rcOutputs[4];
		msg.chan6_raw = (short) rcOutputs[5];
		msg.chan7_raw = (short) rcOutputs[6];
		msg.chan8_raw = (short) rcOutputs[7];
		msg.target_system = 1;
		msg.target_component = 1;
		drone.MavClient.sendMavPacket(msg.pack());
	}
}
