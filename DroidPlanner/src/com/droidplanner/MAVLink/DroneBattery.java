package com.droidplanner.MAVLink;

public class DroneBattery extends DroneVariable{	
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
}