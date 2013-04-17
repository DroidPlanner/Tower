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
import com.google.android.gms.maps.model.LatLng;


public class MavLinkMsgHandler {

	private Drone drone;

	public MavLinkMsgHandler(Drone drone) {
		this.drone = drone;
	}
	
	public void receiveData(MAVLinkMessage msg) {
		switch (msg.msgid) {
		case msg_attitude.MAVLINK_MSG_ID_ATTITUDE:
			msg_attitude m_att = (msg_attitude) msg;
			drone.roll = (m_att.roll * 180.0 / Math.PI);
			drone.pitch = (m_att.pitch * 180.0 / Math.PI);
			drone.yaw = (m_att.yaw * 180.0 / Math.PI);
			if (drone.hudListner != null)
				drone.hudListner.onDroneUpdate();
			break;	
		case msg_vfr_hud.MAVLINK_MSG_ID_VFR_HUD:
			msg_vfr_hud m_hud = (msg_vfr_hud) msg;
			drone.altitude = m_hud.alt;
			drone.groundSpeed = m_hud.groundspeed;
			drone.airSpeed = m_hud.airspeed;
			if (drone.hudListner != null)
				drone.hudListner.onDroneUpdate();						
			break;
		case msg_mission_current.MAVLINK_MSG_ID_MISSION_CURRENT:
			drone.wpno = ((msg_mission_current) msg).seq;
			if (drone.hudListner != null)
				drone.hudListner.onDroneUpdate();
			break;
		case msg_nav_controller_output.MAVLINK_MSG_ID_NAV_CONTROLLER_OUTPUT:
			msg_nav_controller_output m_nav = (msg_nav_controller_output) msg;
			drone.disttowp = m_nav.wp_dist;
			drone.targetAltitude = m_nav.alt_error+drone.altitude;
			drone.targetSpeed = m_nav.aspd_error + drone.airSpeed;
			if (drone.hudListner != null)
				drone.hudListner.onDroneUpdate();
			break;
		case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT:
			msg_heartbeat msg_heart = (msg_heartbeat) msg;
			
			if(drone.type != msg_heart.type){
				drone.type = msg_heart.type;
				if(drone.typeListner != null){
					drone.typeListner.onDroneTypeChanged();
				}
			}
			
			drone.armed = (msg_heart.base_mode & (byte)MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED) == (byte)MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED;
			drone.failsafe = msg_heart.system_status == (byte)MAV_STATE.MAV_STATE_CRITICAL;				
            
            ApmModes newMode;
            newMode = ApmModes.getMode(msg_heart.custom_mode,drone.type);
			if (!drone.mode.equals(newMode)) {
				drone.mode = newMode;
				if (drone.hudListner != null)
					drone.hudListner.onDroneUpdate();			
			}
			break;
		case msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT:
			drone.position = new LatLng(((msg_global_position_int)msg).lat/1E7, ((msg_global_position_int)msg).lon/1E7);
			drone.mapListner.onDroneUpdate();
			break;
		case msg_sys_status.MAVLINK_MSG_ID_SYS_STATUS:
			msg_sys_status m_sys = (msg_sys_status) msg;
			drone.battVolt = m_sys.voltage_battery/1000.0;
			drone.battRemain = m_sys.battery_remaining;
			drone.battCurrent = m_sys.current_battery/100.0;
			if (drone.hudListner != null)
				drone.hudListner.onDroneUpdate();
			break;
		case msg_radio.MAVLINK_MSG_ID_RADIO:
			// TODO implement link quality
			break;
		case msg_gps_raw_int.MAVLINK_MSG_ID_GPS_RAW_INT:
			drone.fixType = ((msg_gps_raw_int) msg).fix_type;
			drone.satCount = ((msg_gps_raw_int) msg).satellites_visible;
			if (drone.hudListner != null)
				drone.hudListner.onDroneUpdate();
		}
	}
}
