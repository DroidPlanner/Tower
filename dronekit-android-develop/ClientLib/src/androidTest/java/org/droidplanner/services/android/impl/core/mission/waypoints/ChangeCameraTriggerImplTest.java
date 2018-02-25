package org.droidplanner.services.android.impl.core.mission.waypoints;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;

import junit.framework.TestCase;

import org.droidplanner.services.android.impl.core.mission.MissionImpl;
import org.droidplanner.services.android.impl.core.mission.commands.CameraTriggerImpl;

import java.util.List;

public class ChangeCameraTriggerImplTest extends TestCase {

    public void testPackMissionItem() {
        MissionImpl missionImpl = new MissionImpl(null);
        CameraTriggerImpl item = new CameraTriggerImpl(missionImpl, (12.0));

        List<msg_mission_item> listOfMsg = item.packMissionItem();
        assertEquals(1, listOfMsg.size());

        msg_mission_item msg = listOfMsg.get(0);

        assertEquals(MAV_CMD.MAV_CMD_DO_SET_CAM_TRIGG_DIST, msg.command);
        assertEquals(12.0f, msg.param1);
    }

}
