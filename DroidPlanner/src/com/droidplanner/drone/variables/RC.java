package com.droidplanner.drone.variables;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_rc_channels_raw;
import com.MAVLink.Messages.ardupilotmega.msg_servo_output_raw;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.OnRcDataChangedListner;
import com.droidplanner.drone.DroneVariable;

public class RC extends DroneVariable {

	public OnRcDataChangedListner listner;

	public int in[] = new int[8];
	public int out[] = new int[8];

	public RC(Drone myDrone) {
		super(myDrone);
	}

	public void setListner(OnRcDataChangedListner listner) {
		this.listner = listner;
	}

	public void setRcInputValues(msg_rc_channels_raw msg) {
		in[0] = msg.chan1_raw;
		in[1] = msg.chan2_raw;
		in[2] = msg.chan3_raw;
		in[3] = msg.chan4_raw;
		in[4] = msg.chan5_raw;
		in[5] = msg.chan6_raw;
		in[6] = msg.chan7_raw;
		in[7] = msg.chan8_raw;
		if (listner != null) {
			listner.onNewInputRcData();
		}
	}

	public void setRcOutputValues(msg_servo_output_raw msg) {
		out[0] = msg.servo1_raw;
		out[1] = msg.servo2_raw;
		out[2] = msg.servo3_raw;
		out[3] = msg.servo4_raw;
		out[4] = msg.servo5_raw;
		out[5] = msg.servo6_raw;
		out[6] = msg.servo7_raw;
		out[7] = msg.servo8_raw;
		if (listner != null) {
			listner.onNewOutputRcData();
		}
	}

	@Override
	protected void processMAVLinkMessage(MAVLinkMessage msg) {
		switch (msg.msgid) {
		case msg_rc_channels_raw.MAVLINK_MSG_ID_RC_CHANNELS_RAW:
			setRcInputValues((msg_rc_channels_raw) msg);
			break;
		case msg_servo_output_raw.MAVLINK_MSG_ID_SERVO_OUTPUT_RAW:
			setRcOutputValues((msg_servo_output_raw) msg);
			break;
		}
	}

}
