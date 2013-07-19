package com.droidplanner.MAVLink;

public class DroneBattery extends DroneVariable {
	public double battVolt;
	public double battRemain;
	public double battCurrent;

	public DroneBattery(Drone myDrone, double battVolt, double battRemain,
			double battCurrent) {
		super(myDrone);
		this.battVolt = battVolt;
		this.battRemain = battRemain;
		this.battCurrent = battCurrent;
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

	public void setBatteryState(double battVolt, double battRemain, double battCurrent) {
		if (this.battVolt != battVolt | this.battRemain != battRemain
				| this.battCurrent != battCurrent) {
			myDrone.tts.batteryDischargeNotification(battRemain);
			this.battVolt = battVolt;
			this.battRemain = battRemain;
			this.battCurrent = battCurrent;
			myDrone.notifyHudUpdate();
		}
	}
}