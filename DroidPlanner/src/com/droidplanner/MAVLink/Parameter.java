package com.droidplanner.MAVLink;

import java.util.Locale;

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
		return String.format(Locale.US,"%3.3f", value);
	}

}
