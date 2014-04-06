package org.droidplanner.drone.variables.mission.waypoints;

import java.util.List;

import org.droidplanner.R;
import org.droidplanner.drone.variables.mission.Mission;
import org.droidplanner.drone.variables.mission.MissionItem;
import org.droidplanner.fragments.markers.MarkerManager.MarkerSource;
import org.droidplanner.fragments.mission.MissionDetailFragment;
import org.droidplanner.fragments.mission.MissionLandFragment;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_CMD;

public class Land extends SpatialCoordItem implements MarkerSource {
    
    public Land(Mission m) {
        super(m);
    }

	public Land(MissionItem item) {
		super(item);
	}

	public Land(msg_mission_item msg, Mission mission) {
		super(mission, null, null);
		unpackMAVMessage(msg);
	}

	@Override
	public MissionDetailFragment getDetailFragment() {
		MissionDetailFragment fragment = new MissionLandFragment();
		fragment.setItem(this);
		return fragment;
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD.MAV_CMD_NAV_LAND;
		return list;
	} 

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		super.unpackMAVMessage(mavMsg);
		}

	@Override
	protected int getIconDrawable() {
		return R.drawable.ic_wp_land;
	}
	
	@Override
	protected int getIconDrawableSelected() {
		return R.drawable.ic_wp_lan_selected;
	}

}