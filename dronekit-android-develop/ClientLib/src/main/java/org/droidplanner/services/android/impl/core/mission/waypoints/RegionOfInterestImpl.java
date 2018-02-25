package org.droidplanner.services.android.impl.core.mission.waypoints;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;

import org.droidplanner.services.android.impl.core.mission.MissionImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemType;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;

import java.util.List;

public class RegionOfInterestImpl extends SpatialCoordItem {

	public RegionOfInterestImpl(MissionItemImpl item) {
		super(item);
	}
	
	public RegionOfInterestImpl(MissionImpl missionImpl, LatLongAlt coord) {
		super(missionImpl,coord);
	}

	public RegionOfInterestImpl(msg_mission_item msg, MissionImpl missionImpl) {
		super(missionImpl, null);
		unpackMAVMessage(msg);
	}

	/**
	 * @return True if this roi cancels a previously set roi.
	 */
	public boolean isReset(){
		return coordinate.getLatitude() == 0 && coordinate.getLongitude() == 0 && coordinate.getAltitude() == 0;
	}

	@Override
	public List<msg_mission_item> packMissionItem() {
		List<msg_mission_item> list = super.packMissionItem();
		msg_mission_item mavMsg = list.get(0);
		mavMsg.command = MAV_CMD.MAV_CMD_DO_SET_ROI;
		return list;
	}

	@Override
	public void unpackMAVMessage(msg_mission_item mavMsg) {
		super.unpackMAVMessage(mavMsg);
	}

	@Override
	public MissionItemType getType() {
		return MissionItemType.ROI;
	}

}