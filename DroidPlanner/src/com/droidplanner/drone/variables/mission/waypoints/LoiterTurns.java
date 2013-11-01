package com.droidplanner.drone.variables.mission.waypoints;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import com.droidplanner.fragments.mission.MissionDetailFragment;
import com.droidplanner.fragments.mission.MissionLoiterNFragment;
import com.google.android.gms.maps.model.LatLng;

public class LoiterTurns extends Loiter implements MarkerSource {
	private int turns;
	
	public LoiterTurns(LatLng coord, double altitude) {
		super(coord, altitude);
	}

	public int getTurns() {
		return turns;
	}

	public void setTurns(int turns) {
		this.turns = turns;
	}
	
	@Override
	public MissionDetailFragment getDetailFragment() {
		MissionDetailFragment fragment = new MissionLoiterNFragment();
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