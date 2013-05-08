package com.droidplanner.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_command_ack;
import com.MAVLink.Messages.ardupilotmega.msg_command_long;
import com.MAVLink.Messages.ardupilotmega.msg_statustext;
import com.MAVLink.Messages.enums.MAV_CMD;
import com.MAVLink.Messages.enums.MAV_CMD_ACK;
import com.droidplanner.service.MAVLinkClient;

public class CalibrationSetup implements OnClickListener {
	private MAVLinkClient MAV;
	private Context context;

	private int count;

	public CalibrationSetup(MAVLinkClient MAVClient) {
		this.MAV = MAVClient;
	}

	public void startCalibration(Context context) {
		this.context = context;
		sendStartCalibrationMessage();
		count = 0;
	}

	@Override
	public void onClick(DialogInterface dialog, int id) {
		count++;
		sendCalibrationAckMessage(count);
		if (count >= 6) {
			createDialog("Calibration Done!");
			count = 0;
		}
	}

	public void processMessage(MAVLinkMessage msg) {
		if (msg.msgid == msg_statustext.MAVLINK_MSG_ID_STATUSTEXT) {
			msg_statustext statusMsg = (msg_statustext) msg;
			if (statusMsg.getText().contains("Place APM")) {
				createDialog(statusMsg.getText());
			}
		}
	}

	private void sendStartCalibrationMessage() {
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
		MAV.sendMavPacket(msg.pack());
	}

	private void sendCalibrationAckMessage(int count) {
		msg_command_ack msg = new msg_command_ack();
		msg.command = (short) count;
		msg.result = MAV_CMD_ACK.MAV_CMD_ACK_OK;
		MAV.sendMavPacket(msg.pack());
	}

	private void createDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(message);
		builder.setPositiveButton("Ok", this);
		builder.setCancelable(false);
		builder.create();
		builder.show();
	}

}
