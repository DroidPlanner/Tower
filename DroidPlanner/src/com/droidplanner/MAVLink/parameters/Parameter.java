package com.droidplanner.MAVLink.parameters;

import java.text.DecimalFormat;

public class Parameter {
	public String name;
	public double value;
	public int type;
	public int index;
	
	public Parameter(String name, double value, int type, int index) {
		this.name = name;
		this.value = value;
		this.type = type;
		this.index = index;
	}
	
	public String getValue(){
		DecimalFormat format = new DecimalFormat("0.###");
		return format.format(value);		
	}

}
