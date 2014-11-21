package org.droidplanner.core.MAVLink;

import org.droidplanner.core.model.Drone;
import org.droidplanner.core.parameters.Parameter;

import com.MAVLink.common.msg_param_request_list;
import com.MAVLink.common.msg_param_request_read;
import com.MAVLink.common.msg_param_set;

public class MavLinkParameters {
	public static void requestParametersList(Drone drone) {
		msg_param_request_list msg = new msg_param_request_list();
		msg.target_system = drone.getSysid();
		msg.target_component = drone.getCompid();
		drone.getMavClient().sendMavPacket(msg.pack());
	}

	public static void sendParameter(Drone drone, Parameter parameter) {
		msg_param_set msg = new msg_param_set();
		msg.target_system = drone.getSysid();
		msg.target_component = drone.getCompid();
		msg.setParam_Id(parameter.name);
		msg.param_type = (byte) parameter.type;
		msg.param_value = (float) parameter.value;
		drone.getMavClient().sendMavPacket(msg.pack());
	}

	public static void readParameter(Drone drone, String name) {
		msg_param_request_read msg = new msg_param_request_read();
		msg.param_index = -1;
		msg.target_system = drone.getSysid();
		msg.target_component = drone.getCompid();
		msg.setParam_Id(name);
		drone.getMavClient().sendMavPacket(msg.pack());
	}

	public static void readParameter(Drone drone, int index) {
		msg_param_request_read msg = new msg_param_request_read();
		msg.target_system = drone.getSysid();
		msg.target_component = drone.getCompid();
		msg.param_index = (short) index;
		drone.getMavClient().sendMavPacket(msg.pack());
	}
}
