package com.droidplanner.drone;

public class DroneInterfaces {
	public interface MapUpdatedListner {
		public void onDroneUpdate();
	}
	public interface DroneTypeListner {
		public void onDroneTypeChanged();
	}
	public interface HudUpdatedListner {
		public void onDroneUpdate();
	}
}