package com.droidplanner.MAVLink;

import com.MAVLink.Messages.enums.MAV_TYPE;
import com.droidplanner.helpers.TTS;

public class Drone {
	private int type = MAV_TYPE.MAV_TYPE_FIXED_WING;

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

	public interface HudUpdatedListner {
		public void onDroneUpdate();
	}

	public interface MapUpdatedListner {
		public void onDroneUpdate();
	}

	public interface DroneTypeListner {
		public void onDroneTypeChanged();
	}

	public Drone(TTS tts) {
		super();
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

	public void setType(int type) {
		if (this.type != type) {
			this.type = type;
			notifyTypeChanged();
		}
	}

	void notifyPositionChange() {
		if (mapListner != null) {
			mapListner.onDroneUpdate();
		}
	}

	private void notifyTypeChanged() {
		if (typeListner != null) {
			typeListner.onDroneTypeChanged();
		}
	}

	void notifyHudUpdate() {
		if (hudListner != null)
			hudListner.onDroneUpdate();
	}

	public int getType() {
		return type;
	}

}
