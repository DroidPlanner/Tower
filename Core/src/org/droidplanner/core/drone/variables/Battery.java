package org.droidplanner.core.drone.variables;

import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.model.Drone;

public class Battery extends DroneVariable {
	public static final double DANGEROUSLY_LOW_BATTERY_PERCENTAGE = 15.0;
	private double battVolt = -1;
	private double battRemain = -1;
	private double battCurrent = -1;

	private double previousBattRemain = 100.0;

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

	public void setBatteryState(double battVolt, double battRemain, double battCurrent) {
		if (this.battVolt != battVolt | this.battRemain != battRemain
				| this.battCurrent != battCurrent) {
			this.battVolt = battVolt;
			this.battRemain = battRemain;
			this.battCurrent = battCurrent;
			myDrone.notifyDroneEvent(DroneEventsType.BATTERY);
			//if battery crosses dangerously_low_battery_percentage threshold
			if(battRemain < DANGEROUSLY_LOW_BATTERY_PERCENTAGE && previousBattRemain >= DANGEROUSLY_LOW_BATTERY_PERCENTAGE){
				myDrone.notifyDroneEvent(DroneEventsType.WARNING_SIGNAL_WEAK);
			}
			previousBattRemain = battRemain;
		}
	}
}