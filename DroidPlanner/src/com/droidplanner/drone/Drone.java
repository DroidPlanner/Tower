package com.droidplanner.drone;

import android.content.Context;

import com.droidplanner.DroidPlannerApp;
import com.droidplanner.drone.DroneInterfaces.DroneTypeListner;
import com.droidplanner.drone.DroneInterfaces.HudUpdatedListner;
import com.droidplanner.drone.DroneInterfaces.MapUpdatedListner;
import com.droidplanner.helpers.TTS;
import com.droidplanner.service.MAVLinkClient;
import com.droidplanner.waypoints.WaypointMananger;

public class Drone {
	public DroneType type = new DroneType(this);
	public DroneGPS GPS = new DroneGPS(this);
	public DroneSpeed speed = new DroneSpeed(this);
	public DroneState state = new DroneState(this);
	public DroneBattery battery = new DroneBattery(this);
	public DroneMission mission = new DroneMission(this);
	public DroneAltitude altitude = new DroneAltitude(this);
	public DroneOrientation orientation = new DroneOrientation(this);
	public DroneParameters parameterMananger;
	public WaypointMananger waypointMananger;
	public DroneCalibration calibrationSetup;

	TTS tts;
	protected MAVLinkClient MavClient;
	protected Context context;

	private HudUpdatedListner hudListner;
	private MapUpdatedListner mapListner;
	private DroneTypeListner typeListner;

	public Drone(TTS tts, MAVLinkClient MAVClient,
			DroidPlannerApp droidPlannerApp, Context context) {
		this.tts = tts;
		this.MavClient = MAVClient;
		this.context = context;
		waypointMananger = new WaypointMananger(this.MavClient, mission);
		parameterMananger = new DroneParameters(this, MAVClient);
		calibrationSetup = new DroneCalibration(MAVClient);
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
