package com.droidplanner.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_attitude;
import com.MAVLink.Messages.ardupilotmega.msg_heartbeat;
import com.MAVLink.Messages.ardupilotmega.msg_mission_current;
import com.MAVLink.Messages.ardupilotmega.msg_nav_controller_output;
import com.MAVLink.Messages.ardupilotmega.msg_vfr_hud;
import com.droidplanner.R;
import com.droidplanner.helpers.HUDwidget;


public class HudFragment extends Fragment{

	private HUDwidget hudWidget;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.hud_fragment, container, false);
		hudWidget = (HUDwidget) view.findViewById(R.id.hudWidget);
		return view;
	}
	
	
	public void receiveData(MAVLinkMessage msg) {
		switch (msg.msgid) {
		case msg_attitude.MAVLINK_MSG_ID_ATTITUDE:
			msg_attitude m = (msg_attitude) msg;
			hudWidget.newFlightData(m.roll, m.pitch, m.yaw);
			break;
		case msg_vfr_hud.MAVLINK_MSG_ID_VFR_HUD:
			hudWidget.setAltitude(((msg_vfr_hud) msg).alt);
			hudWidget.setGroundSpeed(((msg_vfr_hud) msg).groundspeed);
			hudWidget.setAirSpeed(((msg_vfr_hud) msg).airspeed);
			break;
		case msg_mission_current.MAVLINK_MSG_ID_MISSION_CURRENT:
			hudWidget.setWaypointNumber(((msg_mission_current) msg).seq);
			break;
		case msg_nav_controller_output.MAVLINK_MSG_ID_NAV_CONTROLLER_OUTPUT:
			msg_nav_controller_output m1 = (msg_nav_controller_output) msg;
			hudWidget.setDistanceToWaypoint(m1.wp_dist);
			hudWidget.setAltitudeError(m1.alt_error);
			hudWidget.setSpeedError(m1.aspd_error);
			break;
		case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT:
			hudWidget.setMode(((msg_heartbeat) msg).custom_mode);
			break;
		default:
			break;
		}
	}

}
