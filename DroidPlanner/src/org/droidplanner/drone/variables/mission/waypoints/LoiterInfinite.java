package org.droidplanner.drone.variables.mission.waypoints;

import java.util.List;

import org.droidplanner.drone.variables.mission.Mission;
import org.droidplanner.drone.variables.mission.MissionItem;
import org.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import org.droidplanner.fragments.mission.MissionDetailFragment;
import org.droidplanner.fragments.mission.MissionLoiterFragment;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;

public class LoiterInfinite extends Loiter implements MarkerSource {
    
    public LoiterInfinite(Mission m) {
        super(m);
    }
    
    public LoiterInfinite(msg_mission_item msg, Mission mission) {
        super(mission, null, null);
        unpackMAVMessage(msg);
    }
	
	public LoiterInfinite(MissionItem item) {
		super(item);
	}

	@Override
	public MissionDetailFragment getDetailFragment() {
		MissionDetailFragment fragment = new MissionLoiterFragment();
		fragment.setItem(this);
		return fragment;
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		return super.packMissionItem();
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		super.unpackMAVMessage(mavMsg);
	}

}