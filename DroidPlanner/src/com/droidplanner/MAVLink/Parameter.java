package com.droidplanner.MAVLink;

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

}
