package com.droidplanner.drone.variables;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_sys_status;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneVariable;

public class Battery extends DroneVariable{
	private double battVolt = -1;
	private double battRemain = -1;
	private double battCurrent = -1;

	public Battery(Drone myDrone) {
		super(myDrone);
	}

	public double getBattVolt() {
		return battVolt;
	}

	public double getBattRemain() {
		return battRemain;
	}

	public double getBattCurrent() {
		return battCurrent;
	}

	public void setBatteryState(double battVolt, double battRemain,
			double battCurrent) {
		if (this.battVolt != battVolt | this.battRemain != battRemain
				| this.battCurrent != battCurrent) {
			myDrone.tts.batteryDischargeNotification(battRemain);
			this.battVolt = battVolt;
			this.battRemain = battRemain;
			this.battCurrent = battCurrent;
			myDrone.notifyInfoChange();
		}
	}

	@Override
	protected void processMAVLinkMessage(MAVLinkMessage msg) {
		if (msg.msgid == msg_sys_status.MAVLINK_MSG_ID_SYS_STATUS) {
			msg_sys_status m_sys = (msg_sys_status) msg;
			setBatteryState(m_sys.voltage_battery / 1000.0,
					m_sys.battery_remaining, m_sys.current_battery / 100.0);
		}
	}
}