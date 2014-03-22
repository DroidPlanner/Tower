package org.droidplanner.core.drone;


import org.droidplanner.core.MAVLink.MAVLinkStreams.MAVLinkOutputStream;
import org.droidplanner.core.MAVLink.WaypointMananger;
import org.droidplanner.core.drone.DroneInterfaces.Clock;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.Handler;
import org.droidplanner.core.drone.profiles.Parameters;
import org.droidplanner.core.drone.profiles.Profile;
import org.droidplanner.core.drone.variables.Altitude;
import org.droidplanner.core.drone.variables.Battery;
import org.droidplanner.core.drone.variables.Calibration;
import org.droidplanner.core.drone.variables.GPS;
import org.droidplanner.core.drone.variables.GuidedPoint;
import org.droidplanner.core.drone.variables.HeartBeat;
import org.droidplanner.core.drone.variables.Home;
import org.droidplanner.core.drone.variables.MissionStats;
import org.droidplanner.core.drone.variables.Navigation;
import org.droidplanner.core.drone.variables.Orientation;
import org.droidplanner.core.drone.variables.RC;
import org.droidplanner.core.drone.variables.Radio;
import org.droidplanner.core.drone.variables.Speed;
import org.droidplanner.core.drone.variables.State;
import org.droidplanner.core.drone.variables.StreamRates;
import org.droidplanner.core.drone.variables.Type;
import org.droidplanner.core.mission.Mission;


public class Drone {
	public DroneEvents events = new DroneEvents(this);
	public Type type = new Type(this);
	public Profile profile = new Profile(this);
	public GPS GPS = new GPS(this);
	public RC RC = new RC(this);
	public Speed speed = new Speed(this);
	public Battery battery = new Battery(this);
	public Radio radio = new Radio(this);
	public Home home = new Home(this);
	public Mission mission = new Mission(this);
	public MissionStats missionStats = new MissionStats(this);
	public StreamRates streamRates = new StreamRates(this);
	public Altitude altitude = new Altitude(this);
	public Orientation orientation = new Orientation(this);
	public Navigation navigation = new Navigation(this);
	public GuidedPoint guidedPoint = new GuidedPoint(this);
	public Parameters parameters = new Parameters(this);
	public Calibration calibrationSetup = new Calibration(this);
	public WaypointMananger waypointMananger = new WaypointMananger(this);
	public State state;
	public HeartBeat heartbeat;

	public MAVLinkOutputStream MavClient;
	public Preferences preferences;

	public Drone(MAVLinkOutputStream mavClient, Clock clock, Handler handler, Preferences pref) {
		this.MavClient = mavClient;
		this.preferences = pref;
		state = new State(this,clock);
		heartbeat = new HeartBeat(this,handler);
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
