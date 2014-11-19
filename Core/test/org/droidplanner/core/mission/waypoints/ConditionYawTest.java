package org.droidplanner.core.mission.waypoints;

import java.util.List;

import junit.framework.TestCase;

import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.mission.commands.ConditionYaw;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;

public class ConditionYawTest extends TestCase {

	public void testPackMissionItem() {
		Mission mission = new Mission(null);
		ConditionYaw item = new ConditionYaw(mission, 12,false);

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
