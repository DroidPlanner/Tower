package com.droidplanner.MAVLink;

public class DroneBattery {
	public double battVolt;
	public double battRemain;
	public double battCurrent;

	public DroneBattery(double battVolt, double battRemain, double battCurrent) {
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