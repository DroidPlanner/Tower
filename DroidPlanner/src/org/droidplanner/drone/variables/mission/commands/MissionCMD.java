package org.droidplanner.drone.variables.mission.commands;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.drone.variables.mission.Mission;
import org.droidplanner.drone.variables.mission.MissionItem;
import org.droidplanner.fragments.markers.MarkerManager.MarkerSource;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.google.android.gms.maps.model.LatLng;

public abstract class MissionCMD extends MissionItem{
	
	public MissionCMD(MissionItem item) {
		super(item);
	}
	
	public MissionCMD(Mission mission) {
		super(mission);
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = new ArrayList<msg_mission_item>();
		msg_mission_item mavMsg = new msg_mission_item();
		list.add(mavMsg);
		mavMsg.autocontinue = 1;
		mavMsg.target_component = 1;
		mavMsg.target_system = 1;
		return list;
	}


	@Override
	public List<LatLng> getPath() throws Exception {
		throw new Exception();
	}

    @Override
    public boolean hasCoordinates() {
        return false;
    }

    @Override
	public List<MarkerSource> getMarkers() throws Exception {
		throw new Exception();
	}
	
}