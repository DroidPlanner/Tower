package org.droidplanner.core.drone;

import com.MAVLink.Messages.ardupilotmega.msg_heartbeat;

import org.droidplanner.core.MAVLink.MAVLinkStreams;
import org.droidplanner.core.MAVLink.MAVLinkStreams.MAVLinkOutputStream;
import org.droidplanner.core.MAVLink.WaypointManager;
import org.droidplanner.core.drone.DroneInterfaces.Clock;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.Handler;
import org.droidplanner.core.drone.profiles.Parameters;
import org.droidplanner.core.drone.profiles.VehicleProfile;
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
import org.droidplanner.core.firmware.FirmwareType;
import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.model.AbstractDrone;

public class Drone implements AbstractDrone {

	private final DroneEvents events = new DroneEvents(this);
	private final Type type = new Type(this);
    private VehicleProfile profile;
	private final GPS GPS = new GPS(this);

	private final RC RC = new RC(this);
	private final Speed speed = new Speed(this);
	private final Battery battery = new Battery(this);
	private final Radio radio = new Radio(this);
	private final Home home = new Home(this);
	private final Mission mission = new Mission(this);
	private final MissionStats missionStats = new MissionStats(this);
	private final StreamRates streamRates = new StreamRates(this);
	private final Altitude altitude = new Altitude(this);
	private final Orientation orientation = new Orientation(this);
	private final Navigation navigation = new Navigation(this);
	private final GuidedPoint guidedPoint = new GuidedPoint(this);
	private final Calibration calibrationSetup = new Calibration(this);
	private final WaypointManager waypointManager = new WaypointManager(this);

    private final State state;
	private final HeartBeat heartbeat;
	private final Parameters parameters;

	private final MAVLinkOutputStream MavClient;
	private final Preferences preferences;

	public Drone(MAVLinkOutputStream mavClient, Clock clock, Handler handler, Preferences pref) {
		this.MavClient = mavClient;
		this.preferences = pref;
		state = new State(this, clock, handler);
		heartbeat = new HeartBeat(this, handler);
		parameters = new Parameters(this, handler);
		loadVehicleProfile();
	}

	public void setAltitudeGroundAndAirSpeeds(double altitude, double groundSpeed, double airSpeed,
			double climb) {
		this.altitude.setAltitude(altitude);
		speed.setGroundAndAirSpeeds(groundSpeed, airSpeed, climb);
		events.notifyDroneEvent(DroneEventsType.SPEED);
	}

	public void setDisttowpAndSpeedAltErrors(double disttowp, double alt_error, double aspd_error) {
		missionStats.setDistanceToWp(disttowp);
		altitude.setAltitudeError(alt_error);
		speed.setSpeedError(aspd_error);
		events.notifyDroneEvent(DroneEventsType.ORIENTATION);
	}

    @Override
    public void addDroneListener(DroneInterfaces.OnDroneListener listener) {
        events.addDroneListener(listener);
    }

    @Override
    public void removeDroneListener(DroneInterfaces.OnDroneListener listener) {
        events.removeDroneListener(listener);
    }

    @Override
    public void notifyDroneEvent(DroneEventsType event) {
        events.notifyDroneEvent(event);
    }

    @Override
    public GPS getGps() {
        return GPS;
    }

    @Override
    public int getMavlinkVersion() {
        return heartbeat.getMavlinkVersion();
    }

    @Override
    public void onHeartbeat(msg_heartbeat msg) {
        heartbeat.onHeartbeat(msg);
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public Parameters getParameters() {
        return parameters;
    }

    @Override
    public void setType(int type) {
        this.type.setType(type);
    }

    @Override
    public int getType() {
        return type.getType();
    }

    @Override
    public FirmwareType getFirmwareType() {
        return type.getFirmwareType();
    }

    @Override
    public void loadVehicleProfile() {
        preferences.loadVehicleProfile(getFirmwareType());
    }

    @Override
    public VehicleProfile getVehicleProfile() {
        return profile;
    }

    @Override
    public MAVLinkOutputStream getMavClient() {
        return MavClient;
    }

    @Override
    public Preferences getPreferences() {
        return preferences;
    }

    @Override
    public WaypointManager getWaypointManager() {
        return waypointManager;
    }

    public RC getRC() {
        return RC;
    }

    @Override
    public Speed getSpeed() {
        return speed;
    }

    @Override
    public Battery getBattery() {
        return battery;
    }

    @Override
    public Radio getRadio() {
        return radio;
    }

    @Override
    public Home getHome() {
        return home;
    }

    public Mission getMission() {
        return mission;
    }

    public MissionStats getMissionStats() {
        return missionStats;
    }

    public StreamRates getStreamRates() {
        return streamRates;
    }

    @Override
    public Altitude getAltitude() {
        return altitude;
    }

    @Override
    public Orientation getOrientation() {
        return orientation;
    }

    @Override
    public Navigation getNavigation() {
        return navigation;
    }

    public GuidedPoint getGuidedPoint() {
        return guidedPoint;
    }

    public Calibration getCalibrationSetup() {
        return calibrationSetup;
    }
}
