package org.droidplanner.core.mission;

import java.util.ArrayList;
import java.util.List;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_FRAME;

public abstract class MissionItem implements Comparable<MissionItem> {

	protected Mission mission;

	public MissionItem(Mission mission) {
		this.mission = mission;
	}

	public MissionItem(MissionItem item) {
		this(item.mission);
	}

	/**
	 * Return a new list (one or more) of MAVLinkMessage msg_mission_item that
	 * represent this MissionItem
	 * 
	 * @return
	 */
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = new ArrayList<msg_mission_item>();
		msg_mission_item mavMsg = new msg_mission_item();
		list.add(mavMsg);
		mavMsg.autocontinue = 1;
		mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
		return list;
	}

	/**
	 * Gets data from MAVLinkMessage msg_mission_item for this MissionItem
	 * 
	 * @return
	 */
	public abstract void unpackMAVMessage(msg_mission_item mavMsg);

	public abstract MissionItemType getType();

	public Mission getMission() {
		return mission;
	}

	@Override
	public int compareTo(MissionItem another) {
		return mission.getOrder(this) - mission.getOrder(another);
	}

}
