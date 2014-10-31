package org.droidplanner.core.drone.variables;

import org.droidplanner.core.MAVLink.MavLinkCalibration;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.model.Drone;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_statustext;

public class Calibration extends DroneVariable {
	private Drone myDrone;
	private String mavMsg;
	private boolean calibrating;

	public Calibration(Drone drone) {
		super(drone);
		this.myDrone = drone;
	}

	public boolean startCalibration() {
        if(myDrone.getState().isFlying()) {
            calibrating = false;
        }
        else {
            calibrating = true;
            MavLinkCalibration.sendStartCalibrationMessage(myDrone);
        }
        return calibrating;
	}

	public void sendAckk(int step) {
		MavLinkCalibration.sendCalibrationAckMessage(step, myDrone);
	}

	public void processMessage(MAVLinkMessage msg) {
		if (msg.msgid == msg_statustext.MAVLINK_MSG_ID_STATUSTEXT) {
			msg_statustext statusMsg = (msg_statustext) msg;
			mavMsg = statusMsg.getText();

			if (mavMsg.contains("Calibration"))
				calibrating = false;

			myDrone.notifyDroneEvent(DroneEventsType.CALIBRATION_IMU);
		}
	}

	public String getMessage() {
		return mavMsg;
	}

	public void setCalibrating(boolean flag) {
		calibrating = flag;
	}

	public boolean isCalibrating() {
		return calibrating;
	}
}
