package org.droidplanner.mission;

import java.util.List;


import com.MAVLink.Messages.ardupilotmega.msg_mission_item;

public abstract class MissionItemD implements Comparable<MissionItemD>{

	protected Mission mission;

	public MissionItemD(Mission mission) {
		this.mission = mission;
	}

	public MissionItemD(MissionItemD item) {
		this(item.mission);
	}
	/**
	 * Return a new list (one or more) of MAVLinkMessage msg_mission_item that
	 * represent this MissionItem
	 * 
	 * @return
	 */
	public abstract List<msg_mission_item> packMissionItem();

	/**
	 * Gets data from MAVLinkMessage msg_mission_item for this MissionItem
	 * @return
	 */
	public abstract void unpackMAVMessage(msg_mission_item mavMsg);

	public Mission getMission() {
		return mission;
	}

	@Override
	public int compareTo(MissionItemD another) {
		return mission.getNumber(this) - mission.getNumber(another);
	}

}