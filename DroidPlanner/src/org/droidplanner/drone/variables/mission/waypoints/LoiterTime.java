package org.droidplanner.drone.variables.mission.waypoints;

import java.util.List;

import org.droidplanner.drone.variables.mission.Mission;
import org.droidplanner.drone.variables.mission.MissionItem;
import org.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import org.droidplanner.fragments.mission.MissionDetailFragment;
import org.droidplanner.fragments.mission.MissionLoiterTFragment;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_CMD;

public class LoiterTime extends Loiter implements MarkerSource {
	double time;
	
	public LoiterTime(Mission m) {
	    super(m);
	}
	
	public LoiterTime(MissionItem item) {
		super(item);
	}
	
    public LoiterTime(msg_mission_item msg, Mission mission) {
        super(mission, null, null);
        unpackMAVMessage(msg);
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
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD.MAV_CMD_NAV_LOITER_TIME;
		mavMsg.param1 = (float) getTime();
		return list;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		super.unpackMAVMessage(mavMsg);
		setTime(mavMsg.param1);
	}

	
}