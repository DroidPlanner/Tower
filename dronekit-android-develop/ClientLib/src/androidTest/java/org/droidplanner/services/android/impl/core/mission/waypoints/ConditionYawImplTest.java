package org.droidplanner.services.android.impl.core.mission.waypoints;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;

import junit.framework.TestCase;

import org.droidplanner.services.android.impl.core.mission.MissionImpl;
import org.droidplanner.services.android.impl.core.mission.commands.ConditionYawImpl;

import java.util.List;

public class ConditionYawImplTest extends TestCase {

    public void testPackMissionItem() {
        MissionImpl missionImpl = new MissionImpl(null);
        ConditionYawImpl item = new ConditionYawImpl(missionImpl, 12, false);

        List<msg_mission_item> listOfMsg = item.packMissionItem();
        assertEquals(1, listOfMsg.size());

        msg_mission_item msg = listOfMsg.get(0);

        assertEquals(MAV_CMD.MAV_CMD_CONDITION_YAW, msg.command);
        assertEquals(12.0f, msg.param1);
        assertEquals(0.0f, msg.param2);
        assertEquals(-1.0f, msg.param3);
        assertEquals(0.0f, msg.param4);
    }

}
