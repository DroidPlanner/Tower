package org.droidplanner.core.MAVLink;

import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.parameters.Parameter;

import com.MAVLink.Messages.ardupilotmega.msg_param_request_list;
import com.MAVLink.Messages.ardupilotmega.msg_param_request_read;
import com.MAVLink.Messages.ardupilotmega.msg_param_set;

public class MavLinkParameters {
	public static void requestParametersList(Drone drone) {
		msg_param_request_list msg = new msg_param_request_list();
		msg.target_system = 1;
		msg.target_component = 1;
		drone.MavClient.sendMavPacket(msg.pack());
	}

	public static void sendParameter(Drone drone, Parameter parameter) {
		msg_param_set msg = new msg_param_set();
		msg.target_system = 1;
		msg.target_component = 1;
		msg.setParam_Id(parameter.name);
		msg.param_type = (byte) parameter.type;
		msg.param_value = (float) parameter.value;
		drone.MavClient.sendMavPacket(msg.pack());
	}

	public static void readParameter(Drone drone, String name) {
		msg_param_request_read msg = new msg_param_request_read();
		msg.param_index = -1;
		msg.target_system = 1;
		msg.target_component = 1;
		msg.setParam_Id(name);
		drone.MavClient.sendMavPacket(msg.pack());
	}
}
