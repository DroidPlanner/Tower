package com.droidplanner.MAVLink;

import java.util.ArrayList;

import com.MAVLink.waypoint;
import com.MAVLink.Messages.ApmModes;
import com.MAVLink.Messages.enums.MAV_TYPE;
import com.droidplanner.helpers.TTS;

public class Drone {
	private int type = MAV_TYPE.MAV_TYPE_FIXED_WING;
		
	public DroneGPS GPS = new DroneGPS(this,-1, -1, -1);
	public DroneSpeed speed = new DroneSpeed(this,0, 0, 0, 0);
	public DroneState state = new DroneState(this,false, false, ApmModes.UNKNOWN);
	public DroneBattery battery = new DroneBattery(this,-1, -1,-1);	
	public DroneMission mission = new DroneMission(this,-1,0);	
	public DroneAltitude altitude = new DroneAltitude(this,0, 0);
	public DroneOrientation orientation = new DroneOrientation(this,0, 0, 0);
	
	
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
		this.mission.home = new waypoint(0.0, 0.0, 0.0);
		this.mission.defaultAlt = 50.0;
		this.mission.waypoints = new ArrayList<waypoint>();
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
		this.altitude.altitude = altitude;
		this.speed.groundSpeed = groundSpeed;
		this.speed.airSpeed = airSpeed;
		this.speed.verticalSpeed = climb;
		notifyHudUpdate();
	}

	public void setDisttowpAndSpeedAltErrors(double disttowp, double alt_error,
			double aspd_error) {
		this.mission.disttowp = disttowp;
		altitude.targetAltitude = alt_error + altitude.altitude;
		speed.targetSpeed = aspd_error + speed.airSpeed;
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
