package com.MAVLink;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.MAVLink.Messages.ApmModes;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_attitude;
import com.MAVLink.Messages.ardupilotmega.msg_global_position_int;
import com.MAVLink.Messages.ardupilotmega.msg_gps_raw_int;
import com.MAVLink.Messages.ardupilotmega.msg_heartbeat;
import com.MAVLink.Messages.ardupilotmega.msg_mission_current;
import com.MAVLink.Messages.ardupilotmega.msg_nav_controller_output;
import com.MAVLink.Messages.ardupilotmega.msg_radio;
import com.MAVLink.Messages.ardupilotmega.msg_sys_status;
import com.MAVLink.Messages.ardupilotmega.msg_vfr_hud;
import com.MAVLink.Messages.enums.MAV_MODE_FLAG;
import com.MAVLink.Messages.enums.MAV_STATE;
import com.MAVLink.Messages.enums.MAV_TYPE;
import com.google.android.gms.maps.model.LatLng;

public class Drone {
	public waypoint home;
	public Double defaultAlt;
	public List<waypoint> waypoints;
	
	public double roll = 0, pitch = 0, yaw = 0, altitude = 0, disttowp = 0,
			verticalSpeed = 0, groundSpeed = 0, airSpeed = 0, targetSpeed = 0,
			targetAltitude = 0, battVolt = -1, battRemain = -1,
			battCurrent = -1;
	public int wpno = -1,satCount = -1, fixType = -1, type = MAV_TYPE.MAV_TYPE_FIXED_WING;
	public boolean failsafe = false, armed = false;
	public ApmModes mode = ApmModes.UNKNOWN;
	public LatLng position;
	
	
	private HudUpdatedListner hudListner;
	private MapUpdatedListner mapListner;
	private DroneTypeListner typeListner;
	
	
	public interface HudUpdatedListner{
		public void onDroneUpdate();
	}
	public interface MapUpdatedListner{
		public void onDroneUpdate();
	}
	public interface DroneTypeListner{
		public void onDroneTypeChanged();
	}

	public Drone() {
		super();
		this.home = new waypoint(0.0, 0.0, 0.0);
		this.defaultAlt = 100.0;
		this.waypoints = new ArrayList<waypoint>();
	}
	
	
	public void receiveData(MAVLinkMessage msg) {
		switch (msg.msgid) {
		case msg_attitude.MAVLINK_MSG_ID_ATTITUDE:
			msg_attitude m_att = (msg_attitude) msg;
			roll = (m_att.roll * 180.0 / Math.PI);
			pitch = (m_att.pitch * 180.0 / Math.PI);
			yaw = (m_att.yaw * 180.0 / Math.PI);
			if (hudListner != null)
				hudListner.onDroneUpdate();
			break;	
		case msg_vfr_hud.MAVLINK_MSG_ID_VFR_HUD:
			msg_vfr_hud m_hud = (msg_vfr_hud) msg;
			altitude = m_hud.alt;
			groundSpeed = m_hud.groundspeed;
			airSpeed = m_hud.airspeed;
			if (hudListner != null)
				hudListner.onDroneUpdate();						
			break;
		case msg_mission_current.MAVLINK_MSG_ID_MISSION_CURRENT:
			wpno = ((msg_mission_current) msg).seq;
			if (hudListner != null)
				hudListner.onDroneUpdate();
			break;
		case msg_nav_controller_output.MAVLINK_MSG_ID_NAV_CONTROLLER_OUTPUT:
			msg_nav_controller_output m_nav = (msg_nav_controller_output) msg;
			disttowp = m_nav.wp_dist;
			targetAltitude = m_nav.alt_error+altitude;
			targetSpeed = m_nav.aspd_error + airSpeed;
			if (hudListner != null)
				hudListner.onDroneUpdate();
			break;
		case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT:
			msg_heartbeat msg_heart = (msg_heartbeat) msg;
			
			if(type != msg_heart.type){
				type = msg_heart.type;
				if(typeListner != null){
					typeListner.onDroneTypeChanged();
				}
			}
			
			armed = (msg_heart.base_mode & (byte)MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED) == (byte)MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED;
            failsafe = msg_heart.system_status == (byte)MAV_STATE.MAV_STATE_CRITICAL;				
            
            ApmModes newMode;
            newMode = ApmModes.getMode(msg_heart.custom_mode,type);
			if (!mode.equals(newMode)) {
				mode = newMode;
				if (hudListner != null)
					hudListner.onDroneUpdate();			
			}
			break;
		case msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT:
			position = new LatLng(((msg_global_position_int)msg).lat/1E7, ((msg_global_position_int)msg).lon/1E7);
			mapListner.onDroneUpdate();
			break;
		case msg_sys_status.MAVLINK_MSG_ID_SYS_STATUS:
			msg_sys_status m_sys = (msg_sys_status) msg;
			battVolt = m_sys.voltage_battery/1000.0;
			battRemain = m_sys.battery_remaining;
			battCurrent = m_sys.current_battery/100.0;
			if (hudListner != null)
				hudListner.onDroneUpdate();
			break;
		case msg_radio.MAVLINK_MSG_ID_RADIO:
			// TODO implement link quality
			break;
		case msg_gps_raw_int.MAVLINK_MSG_ID_GPS_RAW_INT:
			fixType = ((msg_gps_raw_int) msg).fix_type;
			satCount = ((msg_gps_raw_int) msg).satellites_visible;
			if (hudListner != null)
				hudListner.onDroneUpdate();
		}
	}
	
	
	public void addWaypoints(List<waypoint> points) {
		waypoints.addAll(points);
	}

	public void addWaypoint(Double Lat, Double Lng, Double h) {
		waypoints.add(new waypoint(Lat, Lng, h));
	}

	public void addWaypoint(LatLng coord, Double h) {
		waypoints.add(new waypoint(coord, h));
	}

	public void addWaypoint(LatLng coord) {
		addWaypoint(coord, getDefaultAlt());
	}

	public void clearWaypoints() {
		waypoints.clear();
	}
	
	public String getWaypointData() {
		String waypointData = String.format(Locale.ENGLISH, "Home\t%2.0f\n",
				home.Height);
		waypointData += String.format("Def:\t%2.0f\n", getDefaultAlt());

		int i = 1;
		for (waypoint point : waypoints) {
			waypointData += String.format(Locale.ENGLISH, "WP%02d \t%2.0f\n",
					i++, point.Height);
		}
		return waypointData;
	}

	public List<waypoint> getWaypoints() {
		return waypoints;
	}

	public Double getDefaultAlt() {
		return defaultAlt;
	}

	public void setDefaultAlt(Double defaultAlt) {
		this.defaultAlt = defaultAlt;
	}

	public waypoint getHome() {
		return home;
	}

	public waypoint getLastWaypoint() {
		if (waypoints.size() > 0)
			return waypoints.get(waypoints.size() - 1);
		else
			return home;
	}

	public void setHome(waypoint home) {
		this.home = home;
	}
	
	public void setHome(LatLng home) {
		this.home.coord = home;
	}

	public void moveWaypoint(LatLng coord, int number) {
		waypoints.get(number).coord = coord;
	}


	public List<LatLng> getAllCoordinates() {
		List<LatLng> result = new ArrayList<LatLng>();
		for (waypoint point : waypoints) {
			result.add(point.coord);
		}
		result.add(home.coord);
		return result;
	}

	public static boolean isCopter(int type){
		switch (type) {
		case MAV_TYPE.MAV_TYPE_TRICOPTER:
		case MAV_TYPE.MAV_TYPE_QUADROTOR:
		case MAV_TYPE.MAV_TYPE_HEXAROTOR:
		case MAV_TYPE.MAV_TYPE_OCTOROTOR:
		case MAV_TYPE.MAV_TYPE_HELICOPTER:
			return true;
		case MAV_TYPE.MAV_TYPE_FIXED_WING:
		default:
			return false;
		}
	}

	
	public void setHudListner(HudUpdatedListner listner){
		hudListner = listner;
	}
	
	public void setMapListner(MapUpdatedListner listner){
		mapListner = listner;
	}
	
	public void setDroneTypeChangedListner(DroneTypeListner listner){
		typeListner = listner;
	}

	
}
