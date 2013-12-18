package com.droidplanner.calibration;

import java.util.ArrayList;
import java.util.List;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.ardupilotmega.msg_param_value;
import com.droidplanner.MAVLink.MavLinkParameters;
import com.droidplanner.drone.Drone;
import com.droidplanner.parameters.Parameter;

public class CalParameters{
	private Drone myDrone;
	protected List<String>calParameterNames = new ArrayList<String>();
	protected List<Parameter>calParameterItems = new ArrayList<Parameter>();
	
	public CalParameters(Drone myDrone) {
		this.myDrone = myDrone;
		
	}

	public boolean processMessage(MAVLinkMessage msg) {
		if (msg.msgid == msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE) {
			processReceivedParam((msg_param_value) msg);
			return true;
		}
		return false;
	}
	
	protected void processReceivedParam(msg_param_value m_value) {
		Parameter param = new Parameter(m_value);
		calParameterItems.add(param);
		readCaliberationParameter(calParameterItems.size());
	}

	public void getCaliberationParameters(){
		for(@SuppressWarnings("unused") Parameter param : calParameterItems){
			param = null;
		}
		calParameterItems.clear();
		readCaliberationParameter(calParameterItems.size());
	}
	
	private void readCaliberationParameter(int seq){
		if(seq>calParameterNames.size())
			return;
		readParameter(calParameterNames.get(seq));
	}
	
	public void sendParameter(Parameter parameter) {
		MavLinkParameters.sendParameter(myDrone, parameter);
	}
    
	public void readParameter(String name) {
		MavLinkParameters.readParameter(myDrone, name);
	}
}
