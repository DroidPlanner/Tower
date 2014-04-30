package org.droidplanner.core.drone;

import org.droidplanner.core.MAVLink.MAVLinkStreams.MAVLinkOutputStream;
import org.droidplanner.core.MAVLink.WaypointManager;
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

	public final DroneEvents events = new DroneEvents(this);
	public final Type type = new Type(this);
	public final Profile profile = new Profile(this);
	public final GPS GPS = new GPS(this);
	public final RC RC = new RC(this);
	public final Speed speed = new Speed(this);
	public final Battery battery = new Battery(this);
	public final Radio radio = new Radio(this);
	public final Home home = new Home(this);
	public final Mission mission = new Mission(this);
	public final MissionStats missionStats = new MissionStats(this);
	public final StreamRates streamRates = new StreamRates(this);
	public final Altitude altitude = new Altitude(this);
	public final Orientation orientation = new Orientation(this);
	public final Navigation navigation = new Navigation(this);
	public final GuidedPoint guidedPoint = new GuidedPoint(this);
	public final Parameters parameters = new Parameters(this);
	public final Calibration calibrationSetup = new Calibration(this);
	public final WaypointManager waypointManager = new WaypointManager(this);
	public final State state;
	public final HeartBeat heartbeat;

	public final MAVLinkOutputStream MavClient;
	public final Preferences preferences;

	public Drone(MAVLinkOutputStream mavClient, Clock clock, Handler handler,
			Preferences pref) {
		this.MavClient = mavClient;
		this.preferences = pref;
		state = new State(this, clock);
		heartbeat = new HeartBeat(this, handler);
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
