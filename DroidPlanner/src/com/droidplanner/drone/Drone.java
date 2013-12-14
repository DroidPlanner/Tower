package com.droidplanner.drone;


import android.content.Context;

import com.droidplanner.drone.DroneInterfaces.DroneEventsType;
import com.droidplanner.drone.DroneInterfaces.OnDroneListner;
import com.droidplanner.drone.variables.Altitude;
import com.droidplanner.drone.variables.Battery;
import com.droidplanner.drone.variables.Calibration;
import com.droidplanner.drone.variables.GPS;
import com.droidplanner.drone.variables.GuidedPoint;
import com.droidplanner.drone.variables.Home;
import com.droidplanner.drone.variables.MissionStats;
import com.droidplanner.drone.variables.Navigation;
import com.droidplanner.drone.variables.Orientation;
import com.droidplanner.drone.variables.Parameters;
import com.droidplanner.drone.variables.Profile;
import com.droidplanner.drone.variables.RC;
import com.droidplanner.drone.variables.Radio;
import com.droidplanner.drone.variables.Speed;
import com.droidplanner.drone.variables.State;
import com.droidplanner.drone.variables.Type;
import com.droidplanner.drone.variables.mission.Mission;
import com.droidplanner.drone.variables.mission.WaypointMananger;
import com.droidplanner.helpers.TTS;
import com.droidplanner.service.MAVLinkClient;

public class Drone {
	public Type type = new Type(this);
	public Profile profile = new Profile(this);
	public GPS GPS = new GPS(this);
	public RC RC = new RC(this);
	public Speed speed = new Speed(this);
	public State state = new State(this);
	public Battery battery = new Battery(this);
	public Radio radio = new Radio(this);
	public Home home = new Home(this);
	public Mission mission = new Mission(this);
	public MissionStats missionStats = new MissionStats(this);
	public Altitude altitude = new Altitude(this);
	public Orientation orientation = new Orientation(this);
	public Navigation navigation = new Navigation(this);
	public GuidedPoint guidedPoint = new GuidedPoint(this);
	public Parameters parameters = new Parameters(this);
	public Calibration calibrationSetup = new Calibration(this);
	public WaypointMananger waypointMananger = new WaypointMananger(this);
	public DroneEvents events = new DroneEvents(this);

	public TTS tts;
	public MAVLinkClient MavClient;
	public Context context;

	public Drone(TTS tts, MAVLinkClient mavClient, Context context) {
		this.tts = tts;
		this.MavClient = mavClient;
		this.context = context;

		profile.load();
	}

	public void setAltitudeGroundAndAirSpeeds(double altitude,
			double groundSpeed, double airSpeed, double climb) {
		this.altitude.setAltitude(altitude);
		speed.setGroundAndAirSpeeds(groundSpeed, airSpeed, climb);
		events.notifyDroneEvent(DroneEventsType.SPEED);
	}

	public void setDisttowpAndSpeedAltErrors(double disttowp, double alt_error,
			double aspd_error) {
		missionStats.setDistanceToWp(disttowp);
		altitude.setAltitudeError(alt_error);
		speed.setSpeedError(aspd_error);
		events.notifyDroneEvent(DroneEventsType.ORIENTATION);
	}

}
