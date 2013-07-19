package com.droidplanner.drone;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_statustext;
import com.droidplanner.MAVLink.MavLinkCalibration;
import com.droidplanner.service.MAVLinkClient;

public class DroneCalibration implements OnClickListener {
	private MAVLinkClient MAV;
	private Context context;

	private int count;

	public DroneCalibration(MAVLinkClient MAVClient) {
		this.MAV = MAVClient;
	}

	public void startCalibration(Context context) {
		this.context = context;
		MavLinkCalibration.sendStartCalibrationMessage(MAV);
		count = 0;
	}

	@Override
	public void onClick(DialogInterface dialog, int id) {
		count++;
		MavLinkCalibration.sendCalibrationAckMessage(count, MAV);
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

	private void createDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(message);
		builder.setPositiveButton("Ok", this);
		builder.setCancelable(false);
		builder.create();
		builder.show();
	}

}
