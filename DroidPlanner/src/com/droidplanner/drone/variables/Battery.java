package com.droidplanner.drone.variables;

import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneVariable;

public class Battery extends DroneVariable {
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
			myDrone.onOrientationUpdate();
		}
	}
}