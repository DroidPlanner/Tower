package org.droidplanner.core.mission.waypoints;

import java.util.List;

import junit.framework.TestCase;

import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.mission.commands.CameraTrigger;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;

public class ChangeCameraTriggerTest extends TestCase {

	public void testPackMissionItem() {
		Mission mission = new Mission(null);
		CameraTrigger item = new CameraTrigger(mission, new Length(12.0));

		List<msg_mission_item> listOfMsg = item.packMissionItem();
		assertEquals(1, listOfMsg.size());

		msg_mission_item msg = listOfMsg.get(0);

		assertEquals(MAV_CMD.MAV_CMD_DO_SET_CAM_TRIGG_DIST, msg.command);
		assertEquals(12.0f, msg.param1);
	}

}
