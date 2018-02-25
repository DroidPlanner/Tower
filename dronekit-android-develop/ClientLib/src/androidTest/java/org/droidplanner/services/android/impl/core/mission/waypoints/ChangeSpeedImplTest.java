package org.droidplanner.services.android.impl.core.mission.waypoints;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;

import junit.framework.TestCase;

import org.droidplanner.services.android.impl.core.mission.MissionImpl;
import org.droidplanner.services.android.impl.core.mission.commands.ChangeSpeedImpl;

import java.util.List;

public class ChangeSpeedImplTest extends TestCase {

    public void testPackMissionItem() {
        MissionImpl missionImpl = new MissionImpl(null);
        ChangeSpeedImpl item = new ChangeSpeedImpl(missionImpl, 12.0);

        List<msg_mission_item> listOfMsg = item.packMissionItem();
        assertEquals(1, listOfMsg.size());

        msg_mission_item msg = listOfMsg.get(0);

        assertEquals(MAV_CMD.MAV_CMD_DO_CHANGE_SPEED, msg.command);
        assertEquals(0.0f, msg.x);
        assertEquals(0.0f, msg.y);
        assertEquals(0.0f, msg.z);
        assertEquals(0.0f, msg.param1);
        assertEquals(12.0f, msg.param2);
        assertEquals(0.0f, msg.param3);
        assertEquals(0.0f, msg.param3);
    }

}
