package com.droidplanner.MAVLink.parameters;

import java.text.DecimalFormat;

import com.MAVLink.Messages.ardupilotmega.msg_param_value;

public class Parameter {
	public String name;
	public double value;
	public int type;

	public Parameter(String name, double value, int type) {
		this.name = name;
		this.value = value;
		this.type = type;
	}

	public Parameter(msg_param_value m_value) {
		this(m_value.getParam_Id(), m_value.param_value, m_value.param_type);
	}

	public Parameter(String name, Double value) {
		this(name, value, 0);			// TODO Setting type to Zero may cause an error
	}

	public String getValue() {
		DecimalFormat format = new DecimalFormat("0.###");
		return format.format(value);
	}

}
