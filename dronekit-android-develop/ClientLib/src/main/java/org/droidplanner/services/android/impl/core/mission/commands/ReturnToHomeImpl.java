package org.droidplanner.services.android.impl.core.mission.commands;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_FRAME;

import org.droidplanner.services.android.impl.core.mission.MissionImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemType;

import java.util.List;

public class ReturnToHomeImpl extends MissionCMD {

    private double returnAltitude;

    public ReturnToHomeImpl(MissionItemImpl item) {
        super(item);
        returnAltitude = (0);
    }

    public ReturnToHomeImpl(msg_mission_item msg, MissionImpl missionImpl) {
        super(missionImpl);
        unpackMAVMessage(msg);
    }

    public ReturnToHomeImpl(MissionImpl missionImpl) {
        super(missionImpl);
        returnAltitude = (0.0);
    }

    public double getHeight() {
        return returnAltitude;
    }

    public void setHeight(double altitude) {
        returnAltitude = altitude;
    }

    @Override
    public List<msg_mission_item> packMissionItem() {
        List<msg_mission_item> list = super.packMissionItem();
        msg_mission_item mavMsg = list.get(0);
        mavMsg.command = MAV_CMD.MAV_CMD_NAV_RETURN_TO_LAUNCH;
        mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
        mavMsg.z = (float) returnAltitude;
        return list;
    }

    @Override
    public void unpackMAVMessage(msg_mission_item mavMessageItem) {
        returnAltitude = (mavMessageItem.z);
    }

    @Override
    public MissionItemType getType() {
        return MissionItemType.RTL;
    }

}
