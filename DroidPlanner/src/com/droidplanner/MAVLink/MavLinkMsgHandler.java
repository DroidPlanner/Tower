package com.droidplanner.MAVLink;

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
import com.droidplanner.drone.Drone;
import com.google.android.gms.maps.model.LatLng;

public class MavLinkMsgHandler {

	private Drone drone;

	public MavLinkMsgHandler(Drone drone) {
		this.drone = drone;
	}

	public void receiveData(MAVLinkMessage msg) {
		drone.waypointMananger.processMessage(msg);
		drone.parameters.processMessage(msg);
		drone.calibrationSetup.processMessage(msg);

		switch (msg.msgid) {
		case msg_attitude.MAVLINK_MSG_ID_ATTITUDE:
			msg_attitude m_att = (msg_attitude) msg;
			drone.orientation.setRollPitchYaw(m_att.roll * 180.0 / Math.PI,
					m_att.pitch * 180.0 / Math.PI, m_att.yaw * 180.0 / Math.PI);
			break;
		case msg_vfr_hud.MAVLINK_MSG_ID_VFR_HUD:
			msg_vfr_hud m_hud = (msg_vfr_hud) msg;
			drone.setAltitudeGroundAndAirSpeeds(m_hud.alt, m_hud.groundspeed,
					m_hud.airspeed, m_hud.climb);
			break;
		case msg_mission_current.MAVLINK_MSG_ID_MISSION_CURRENT:
			drone.missionStats.setWpno(((msg_mission_current) msg).seq);
			break;
		case msg_nav_controller_output.MAVLINK_MSG_ID_NAV_CONTROLLER_OUTPUT:
			msg_nav_controller_output m_nav = (msg_nav_controller_output) msg;
			drone.setDisttowpAndSpeedAltErrors(m_nav.wp_dist, m_nav.alt_error,
					m_nav.aspd_error);
			break;
		case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT:
			msg_heartbeat msg_heart = (msg_heartbeat) msg;
			drone.type.setType(msg_heart.type);
			drone.state
					.setArmedAndFailsafe(
							(msg_heart.base_mode & (byte) MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED) == (byte) MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED,
							msg_heart.system_status == (byte) MAV_STATE.MAV_STATE_CRITICAL);
			ApmModes newMode;
			newMode = ApmModes.getMode(msg_heart.custom_mode,
					drone.type.getType());
			drone.state.setMode(newMode);
			break;
		case msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT:
			drone.GPS.setPosition(new LatLng(
					((msg_global_position_int) msg).lat / 1E7,
					((msg_global_position_int) msg).lon / 1E7));
			break;
		case msg_sys_status.MAVLINK_MSG_ID_SYS_STATUS:
			msg_sys_status m_sys = (msg_sys_status) msg;
			drone.battery.setBatteryState(m_sys.voltage_battery / 1000.0,
					m_sys.battery_remaining, m_sys.current_battery / 100.0);
			break;
		case msg_radio.MAVLINK_MSG_ID_RADIO:
			// TODO implement link quality
			break;
		case msg_gps_raw_int.MAVLINK_MSG_ID_GPS_RAW_INT:
			drone.GPS.setGpsState(((msg_gps_raw_int) msg).fix_type,
					((msg_gps_raw_int) msg).satellites_visible,
					((msg_gps_raw_int) msg).eph);
		}
	}
}
