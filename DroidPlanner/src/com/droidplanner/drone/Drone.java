package com.droidplanner.drone;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.droidplanner.MAVLink.MavLinkMsgHandler;
import com.droidplanner.drone.DroneInterfaces.DroneTypeListner;
import com.droidplanner.drone.DroneInterfaces.HomeDistanceChangedListner;
import com.droidplanner.drone.DroneInterfaces.HudUpdatedListner;
import com.droidplanner.drone.DroneInterfaces.InfoListner;
import com.droidplanner.drone.DroneInterfaces.MapConfigListener;
import com.droidplanner.drone.DroneInterfaces.MapUpdatedListner;
import com.droidplanner.drone.DroneInterfaces.OnTuningDataListner;
import com.droidplanner.drone.DroneInterfaces.VehicleTypeListener;
import com.droidplanner.drone.variables.Altitude;
import com.droidplanner.drone.variables.Battery;
import com.droidplanner.drone.variables.Calibration;
import com.droidplanner.drone.variables.GPS;
import com.droidplanner.drone.variables.GuidedPoint;
import com.droidplanner.drone.variables.GuidedPoint.OnGuidedListener;
import com.droidplanner.drone.variables.Home;
import com.droidplanner.drone.variables.MissionStats;
import com.droidplanner.drone.variables.Navigation;
import com.droidplanner.drone.variables.Orientation;
import com.droidplanner.drone.variables.Parameters;
import com.droidplanner.drone.variables.RC;
import com.droidplanner.drone.variables.Profile;
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
	private List<InfoListner> infoListeners = new ArrayList<InfoListner>();

	public TTS tts;
	public MAVLinkClient MavClient;
	public Context context;

	private HudUpdatedListner hudListner;
	private MapUpdatedListner mapListner;
	private MapConfigListener mapConfigListener;
	private DroneTypeListner typeListner;
    private VehicleTypeListener vehicleTypeListener;
	private HomeDistanceChangedListner homeChangedListner;
	private OnGuidedListener guidedListner;
	public OnTuningDataListner tuningDataListner;
	
	public Drone(TTS tts, MAVLinkClient mavClient, Context context) {
		this.tts = tts;
		this.MavClient = mavClient;
		this.context = context;
		
        profile.load();
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

    public void setVehicleTypeListener(VehicleTypeListener vehicleTypeListener) {
        this.vehicleTypeListener = vehicleTypeListener;
    }

	public void addInfoListener(InfoListner listener) {
		if(listener!=null)
			infoListeners.add(listener);
	}

	public void removeInfoListener(InfoListner listener) {
		if(listener!=null && infoListeners.contains(listener))
			infoListeners.remove(listener);
	}

	public void setHomeChangedListner(HomeDistanceChangedListner listner) {
		homeChangedListner = listner;		
	}
	
	public void setTuningDataListner(OnTuningDataListner listner) {
		tuningDataListner = listner;
	}

	public void setGuidedPointListner(OnGuidedListener listner) {
		guidedListner = listner;		
	}

	public void setAltitudeGroundAndAirSpeeds(double altitude,
			double groundSpeed, double airSpeed, double climb) {
		this.altitude.setAltitude(altitude);
		speed.setGroundAndAirSpeeds(groundSpeed, airSpeed, climb);
		onSpeedAltitudeAndClimbRateUpdate();
	}

	public void setDisttowpAndSpeedAltErrors(double disttowp, double alt_error,
			double aspd_error) {
		missionStats.setDistanceToWp(disttowp);
		altitude.setAltitudeError(alt_error);
		speed.setSpeedError(aspd_error);
		onOrientationUpdate();
	}

	public void notifyPositionChange() {
		if (mapListner != null) {
			mapListner.onDroneUpdate();
		}
	}
	
	public void notifyGuidedPointChange() {
		if (guidedListner != null) {
			guidedListner.onGuidedPoint();
		}
	}

	public void notifyInfoChange() {
		if (infoListeners.size()>0) {
			for(InfoListner listener : infoListeners){
				listener.onInfoUpdate();
			}
		}
	}
		
	public void notifyDistanceToHomeChange() {
		if (homeChangedListner!= null) {
			homeChangedListner.onDistanceToHomeHasChanged();
		}
	}

	public void notifyTypeChanged() {
        profile.load();

		if (typeListner != null) {
			typeListner.onDroneTypeChanged();
		}
		if (mapListner != null) {
			mapListner.onDroneTypeChanged();
		}
		
	}

    public void notifyVehicleTypeChanged() {
        profile.load();

        if (vehicleTypeListener != null)
            vehicleTypeListener.onVehicleTypeChanged();
    }

	public void onOrientationUpdate() {
		if (hudListner != null)
			hudListner.onOrientationUpdate();
	}
	
	public void onSpeedAltitudeAndClimbRateUpdate() {
		if (hudListner != null)
			hudListner.onSpeedAltitudeAndClimbRateUpdate();
	}

	public void notifyMapTypeChanged() {
		if (mapConfigListener != null)
			mapConfigListener.onMapTypeChanged();
	}

	public void setMavLinkMsgHandler(MavLinkMsgHandler mavLinkMsgHandler) {
		mavLinkMsgHandler.registerMsgListeners(battery);
		mavLinkMsgHandler.registerMsgListeners(radio);
		mavLinkMsgHandler.registerMsgListeners(GPS);
	}

}
