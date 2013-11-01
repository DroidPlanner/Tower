package com.droidplanner.drone.variables.mission.waypoints;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.droidplanner.fragments.mission.MissionDetailFragment;
import com.droidplanner.fragments.mission.MissionLoiterTFragment;
import com.google.android.gms.maps.model.LatLng;

public class LoiterTime extends Loiter implements MarkerSource {
	double time;
	
	public LoiterTime(LatLng coord, double altitude) {
		super(coord, altitude);
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}
	
	@Override
	public MissionDetailFragment getDetailFragment() {
		MissionDetailFragment fragment = new MissionLoiterTFragment();
		fragment.setItem(this);
		return fragment;
	}

	@Override
	public msg_mission_item packMissionItem() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMessageItem) {
		// TODO Auto-generated method stub
		
	}

	
}