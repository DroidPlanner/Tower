package com.droidplanner.drone;


import android.content.Context;

import com.droidplanner.drone.DroneInterfaces.DroneTypeListner;
import com.droidplanner.drone.DroneInterfaces.HudUpdatedListner;
import com.droidplanner.drone.DroneInterfaces.MapUpdatedListner;
import com.droidplanner.drone.DroneInterfaces.MapConfigListener;
import com.droidplanner.drone.DroneInterfaces.ModeChangedListener;
import com.droidplanner.drone.variables.Altitude;
import com.droidplanner.drone.variables.Battery;
import com.droidplanner.drone.variables.Calibration;
import com.droidplanner.drone.variables.GPS;
import com.droidplanner.drone.variables.GuidedPoint;
import com.droidplanner.drone.variables.Mission;
import com.droidplanner.drone.variables.Orientation;
import com.droidplanner.drone.variables.Parameters;
import com.droidplanner.drone.variables.Speed;
import com.droidplanner.drone.variables.State;
import com.droidplanner.drone.variables.Type;
import com.droidplanner.drone.variables.WaypointMananger;
import com.droidplanner.file.IO.VehicleProfile;
import com.droidplanner.helpers.TTS;
import com.droidplanner.service.MAVLinkClient;

import java.util.Map;

public class Drone {
	public Type type = new Type(this);
	public GPS GPS = new GPS(this);
	public Speed speed = new Speed(this);
	public State state = new State(this);
	public Battery battery = new Battery(this);
	public Mission mission = new Mission(this);
	public Altitude altitude = new Altitude(this);
	public Orientation orientation = new Orientation(this);
	public GuidedPoint guidedPoint = new GuidedPoint(this);
	public Parameters parameters = new Parameters(this);
	public Calibration calibrationSetup = new Calibration(this);
	public WaypointMananger waypointMananger = new WaypointMananger(this);

	public TTS tts;
	public MAVLinkClient MavClient;
	public Context context;

    public Map<String, VehicleProfile> vehicleProfiles;

	private HudUpdatedListner hudListner;
	private MapUpdatedListner mapListner;
	private MapConfigListener mapConfigListener;
	private DroneTypeListner typeListner;
	private ModeChangedListener modeChangedListener;

	public Drone(TTS tts, MAVLinkClient mavClient, Context context) {
		this.tts = tts;
		this.MavClient = mavClient;
		this.context = context;
	}

	public void setHudListner(HudUpdatedListner listner) {
		hudListner = listner;
	}

	public void setMapListner(MapUpdatedListner listner) {
		mapListner = listner;
	}

	public void setMapConfigListener(MapConfigListener mapConfigListener) {
		this.mapConfigListener = mapConfigListener;
	}

	public void setDroneTypeChangedListner(DroneTypeListner listner) {
		typeListner = listner;
	}

	public void setModeChangedListener(ModeChangedListener listener)
	{
		this.modeChangedListener = listener;
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

	public void notifyPositionChange() {
		if (mapListner != null) {
			mapListner.onDroneUpdate();
		}
	}

	public void notifyTypeChanged() {
		if (typeListner != null) {
			typeListner.onDroneTypeChanged();
		}
	}

	public void notifyHudUpdate() {
		if (hudListner != null)
			hudListner.onDroneUpdate();
	}

	public void notifyMapTypeChanged() {
		if (mapConfigListener != null)
			mapConfigListener.onMapTypeChanged();
	}

	public void notifyModeChanged()
	{
		if (modeChangedListener != null)
			modeChangedListener.onModeChanged();
	}
}
