package com.droidplanner.drone;

import com.droidplanner.drone.DroneInterfaces.DroneTypeListner;
import com.droidplanner.drone.DroneInterfaces.HudUpdatedListner;
import com.droidplanner.drone.DroneInterfaces.MapUpdatedListner;
import com.droidplanner.helpers.TTS;

public class Drone {
	public DroneType type = new DroneType(this);
	public DroneGPS GPS = new DroneGPS(this);
	public DroneSpeed speed = new DroneSpeed(this);
	public DroneState state = new DroneState(this);
	public DroneBattery battery = new DroneBattery(this);
	public DroneMission mission = new DroneMission(this);
	public DroneAltitude altitude = new DroneAltitude(this);
	public DroneOrientation orientation = new DroneOrientation(this);

	private HudUpdatedListner hudListner;
	private MapUpdatedListner mapListner;
	private DroneTypeListner typeListner;
	TTS tts;

	public Drone(TTS tts) {
		this.tts = tts;
	}

	public void setHudListner(HudUpdatedListner listner) {
		hudListner = listner;
	}

	public void setMapListner(MapUpdatedListner listner) {
		mapListner = listner;
	}

	public void setDroneTypeChangedListner(DroneTypeListner listner) {
		typeListner = listner;
	}

	public void setAltitudeGroundAndAirSpeeds(double altitude,
			double groundSpeed, double airSpeed, double climb) {
		this.altitude.setAltitude(altitude);
		speed.setGroundAndAirSpeeds(groundSpeed, airSpeed, climb);
		notifyHudUpdate();
	}

	public void setDisttowpAndSpeedAltErrors(double disttowp, double alt_error,
			double aspd_error) {
		mission.setDistanceToWp(disttowp);
		altitude.setAltitudeError(alt_error);
		speed.setSpeedError(aspd_error);
		notifyHudUpdate();
	}

	void notifyPositionChange() {
		if (mapListner != null) {
			mapListner.onDroneUpdate();
		}
	}

	void notifyTypeChanged() {
		if (typeListner != null) {
			typeListner.onDroneTypeChanged();
		}
	}

	void notifyHudUpdate() {
		if (hudListner != null)
			hudListner.onDroneUpdate();
	}
}
