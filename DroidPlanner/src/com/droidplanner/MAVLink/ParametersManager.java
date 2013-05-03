package com.droidplanner.MAVLink;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_param_request_list;
import com.MAVLink.Messages.ardupilotmega.msg_param_value;
import com.droidplanner.service.MAVLinkClient;

/**
 * Class to manage the communication of parameters to the MAV.
 * 
 * Should be initialized with a MAVLink Object, so the manager can send messages
 * via the MAV link. The function processMessage must be called with every new
 * MAV Message.
 * 
 */
public class ParametersManager {
	public void getAllParameters() {
		parameters.clear();
		requestParametersList();
	}

	public interface OnParameterManagerListner {
		public abstract void onParametersReceived(List<Parameter> parameters);
	}

	MAVLinkClient MAV;
	private OnParameterManagerListner listner;
	private List<Parameter> parameters;

	enum waypointStates {
		IDLE
	}

	waypointStates state = waypointStates.IDLE;

	public ParametersManager(MAVLinkClient MAV,
			OnParameterManagerListner listner) {
		this.MAV = MAV;
		this.listner = listner;
		parameters = new ArrayList<Parameter>();
	}

	/**
	 * Try to process a Mavlink message if it is a parameter related message
	 * 
	 * @param msg
	 *            Mavlink message to process
	 * @return Returns true if the message has been processed
	 */
	public boolean processMessage(MAVLinkMessage msg) {
		switch (state) {
		default:
		case IDLE:
			break;
		}

		if (msg.msgid == msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE) {
			processReceivedParam((msg_param_value) msg);
			return true;
		}
		return false;
	}

	private void processReceivedParam(msg_param_value m_value) {
		Log.d("PARM", m_value.toString());
		Parameter param = new Parameter(m_value.getParam_Id(),m_value.param_value,m_value.param_type,m_value.param_index);
		parameters.add(param);
		if (m_value.param_index == m_value.param_count - 1) {
			listner.onParametersReceived(parameters);
		}
	}

	private void requestParametersList() {
		msg_param_request_list msg = new msg_param_request_list();
		msg.target_system = 1;
		msg.target_component = 1;
		MAV.sendMavPacket(msg.pack());
	}

}
