package org.droidplanner.services.android.impl.core.mission.survey;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_FRAME;

import org.droidplanner.services.android.impl.core.mission.MissionImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemType;
import com.o3dr.services.android.lib.coordinate.LatLong;

import java.util.List;

public class SplineSurveyImpl extends SurveyImpl {

    public SplineSurveyImpl(MissionImpl missionImpl, List<LatLong> points) {
        super(missionImpl, points);
    }

    @Override
    protected msg_mission_item getSurveyPoint(LatLong point, double altitude) {
        msg_mission_item mavMsg = new msg_mission_item();
        mavMsg.autocontinue = 1;
        mavMsg.frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
        mavMsg.command = MAV_CMD.MAV_CMD_NAV_SPLINE_WAYPOINT;
        mavMsg.x = (float) point.getLatitude();
        mavMsg.y = (float) point.getLongitude();
        mavMsg.z = (float) altitude;
        mavMsg.param1 = 0f;
        mavMsg.param2 = 0f;
        mavMsg.param3 = 0f;
        mavMsg.param4 = 0f;
        return mavMsg;
    }

    @Override
    public MissionItemType getType() {
        return MissionItemType.SPLINE_SURVEY;
    }

}
