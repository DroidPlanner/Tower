package org.droidplanner.desktop.logic;

public class Clock implements org.droidplanner.core.drone.DroneInterfaces.Clock {
	@Override
	public long elapsedRealtime() {
		return System.currentTimeMillis();
	}
};