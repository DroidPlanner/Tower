package org.droidplanner.services.android.impl.core.mission.commands;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_FRAME;

import org.droidplanner.services.android.impl.core.mission.MissionImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemType;

import java.util.List;

public class TakeoffImpl extends MissionCMD {

    public static final double DEFAULT_TAKEOFF_ALTITUDE = 10.0;

    private double finishedAlt = 10;
    private double pitch = 0;

    public TakeoffImpl(MissionItemImpl item) {
        super(item);
    }

    public TakeoffImpl(msg_mission_item msg, MissionImpl missionImpl) {
        super(missionImpl);
        unpackMAVMessage(msg);
    }

    public TakeoffImpl(MissionImpl missionImpl, double altitude) {
        super(missionImpl);
        this.finishedAlt = altitude;
        this.pitch = 0;
    }

    public TakeoffImpl(MissionImpl missionImpl, double altitude, double pitch) {
        super(missionImpl);
        this.finishedAlt = altitude;
        this.pitch = pitch;
    }

    @Override
    public List<msg_mission_item> packMissionItem() {
        List<msg_mission_item> list = super.packMissionItem();
        msg_mission_item mavMsg = list.get(0);
        mavMsg.command = MAV_CMD.MAV_CMD_NAV_TAKEOFF;
        mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
        mavMsg.z = (float) finishedAlt;
        if (pitch > 0)
            mavMsg.param1 = (float) pitch;
        return list;
    }

    @Override
    public void unpackMAVMessage(msg_mission_item mavMsg) {
        finishedAlt = mavMsg.z;
        pitch = mavMsg.param1;
    }

    @Override
    public MissionItemType getType() {
        return MissionItemType.TAKEOFF;
    }

    public double getFinishedAlt() {
        return finishedAlt;
    }

    public void setFinishedAlt(double finishedAlt) {
        this.finishedAlt = finishedAlt;
    }

    public double getPitch() {
        return pitch;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }
}
