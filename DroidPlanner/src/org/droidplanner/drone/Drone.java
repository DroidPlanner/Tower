package org.droidplanner.drone;


import org.droidplanner.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.drone.variables.Altitude;
import org.droidplanner.drone.variables.Battery;
import org.droidplanner.drone.variables.Calibration;
import org.droidplanner.drone.variables.GPS;
import org.droidplanner.drone.variables.GuidedPoint;
import org.droidplanner.drone.variables.Home;
import org.droidplanner.drone.variables.MissionStats;
import org.droidplanner.drone.variables.Navigation;
import org.droidplanner.drone.variables.Orientation;
import org.droidplanner.drone.variables.Parameters;
import org.droidplanner.drone.variables.Profile;
import org.droidplanner.drone.variables.RC;
import org.droidplanner.drone.variables.Radio;
import org.droidplanner.drone.variables.Speed;
import org.droidplanner.drone.variables.State;
import org.droidplanner.drone.variables.Type;
import org.droidplanner.drone.variables.mission.Mission;
import org.droidplanner.drone.variables.mission.WaypointMananger;
import org.droidplanner.helpers.TTS;
import org.droidplanner.service.MAVLinkClient;

import android.content.Context;


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
		this.MavClient = mavClient;
		this.context = context;
		this.tts = tts;
		events.addDroneListener(tts);
		
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
