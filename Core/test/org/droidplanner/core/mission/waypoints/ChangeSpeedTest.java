package org.droidplanner.core.mission.waypoints;

import java.util.List;

import junit.framework.TestCase;

import org.droidplanner.core.helpers.units.Speed;
import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.mission.commands.ChangeSpeed;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.MAVLink.Messages.enums.MAV_CMD;

public class ChangeSpeedTest extends TestCase {

	public void testPackMissionItem() {
		Mission mission = new Mission(null);
		ChangeSpeed item = new ChangeSpeed(mission, new Speed(12.0));

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
