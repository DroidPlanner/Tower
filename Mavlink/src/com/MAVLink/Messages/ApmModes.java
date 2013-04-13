package com.MAVLink.Messages;

public enum ApmModes {
	MANUAL (0,"Manual"),
	CIRCLE (1,"Circle"),
	STABILIZE (2,"Stabilize"),
	TRAINING (3,"Training"),
	FLY_BY_WIRE_A (5,"FBW A"),
	FLY_BY_WIRE_B (6,"FBW B"),
	AUTO (10,"Auto"),
	RTL (11,"RTL"),
	LOITER (12,"Loiter"),
	GUIDED (15,"Guided"),
	UNKNOWN (15,"Unknown");

	private final int number;
    private final String name;
    
	ApmModes(int number,String name){
		this.number = number;
		this.name = name;
	}
	
	public int getNumber() {
		return number;
	}

	public String getName() {
		return name;
	}
	
	public static ApmModes getMode(int i) {
		for (ApmModes mode : ApmModes.values()) {
			if (i == mode.getNumber()) {
				return mode;
			}
		}
		return UNKNOWN;
	}
	
	public static ApmModes getMode(String str) {
		for (ApmModes mode : ApmModes.values()) {
			if (str.equals(mode.getName())) {
				return mode;
			}
		}
		return UNKNOWN;
	}
	
	
}
