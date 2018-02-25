package org.droidplanner.services.android.impl.core.MAVLink;

import com.MAVLink.common.msg_param_request_list;
import com.MAVLink.common.msg_param_request_read;
import com.MAVLink.common.msg_param_set;

import org.droidplanner.services.android.impl.core.drone.autopilot.MavLinkDrone;
import com.o3dr.services.android.lib.drone.property.Parameter;

public class MavLinkParameters {
	public static void requestParametersList(MavLinkDrone drone) {
		msg_param_request_list msg = new msg_param_request_list();
		msg.target_system = drone.getSysid();
		msg.target_component = drone.getCompid();
		drone.getMavClient().sendMessage(msg, null);
	}

	public static void readParameter(MavLinkDrone drone, String name) {
		msg_param_request_read msg = new msg_param_request_read();
		msg.param_index = -1;
		msg.target_system = drone.getSysid();
		msg.target_component = drone.getCompid();
		msg.setParam_Id(name);
		drone.getMavClient().sendMessage(msg, null);
	}

	public static void readParameter(MavLinkDrone drone, int index) {
		msg_param_request_read msg = new msg_param_request_read();
		msg.target_system = drone.getSysid();
		msg.target_component = drone.getCompid();
		msg.param_index = (short) index;
		drone.getMavClient().sendMessage(msg, null);
	}

	public static void sendParameter(MavLinkDrone drone, Parameter parameter) {
		sendParameter(drone, parameter.getName(), parameter.getType(), (float) parameter.getValue());
	}

	public static void sendParameter(MavLinkDrone drone, String name, int type, float value) {
		msg_param_set msg = new msg_param_set();
		msg.target_system = drone.getSysid();
		msg.target_component = drone.getCompid();
		msg.setParam_Id(name);
		msg.param_type = (byte) type;
		msg.param_value = value;
		drone.getMavClient().sendMessage(msg, null);
	}
}
