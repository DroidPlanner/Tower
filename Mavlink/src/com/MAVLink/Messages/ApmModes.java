package com.MAVLink.Messages;

public class ApmModes {
	public static final int MANUAL = 0;
	public static final int CIRCLE = 1;
	public static final int STABILIZE = 2;
	public static final int TRAINING = 3;
	public static final int FLY_BY_WIRE_A = 5;
	public static final int FLY_BY_WIRE_B = 6;
	public static final int AUTO = 10;
	public static final int RTL = 11;
	public static final int LOITER = 12;
	public static final int GUIDED = 15;

	public static int toInt(String str) {
		if (str.equals("Auto")) {
			return AUTO;
		}
		if (str.equals("Circle")) {
			return CIRCLE;
		}
		if (str.equals("FBW B")) {
			return FLY_BY_WIRE_B;
		}
		if (str.equals("FBW A")) {
			return FLY_BY_WIRE_A;
		}
		if (str.equals("Training")) {
			return TRAINING;
		}
		if (str.equals("Stabilize")) {
			return STABILIZE;
		}
		if (str.equals("Manual")) {
			return MANUAL;
		}
		if (str.equals("RTL")) {
			return RTL;
		}
		return -1;
	}
	
	public static String toString(int m) {
		String mode;
		switch (m) {
		case ApmModes.AUTO:
			mode = "Auto";
			break;
		case ApmModes.CIRCLE:
			mode = "Circle";
			break;
		case ApmModes.FLY_BY_WIRE_A:
			mode = "FBW A";
			break;
		case ApmModes.FLY_BY_WIRE_B:
			mode = "FBW B";
			break;
		case ApmModes.GUIDED:
			mode = "Guided";
			break;
		case ApmModes.LOITER:
			mode = "Loiter";
			break;
		case ApmModes.MANUAL:
			mode = "Manual";
			break;
		case ApmModes.RTL:
			mode = "RTL";
			break;
		case ApmModes.STABILIZE:
			mode = "Stabilize";
			break;
		case ApmModes.TRAINING:
			mode = "Training";
			break;
		default:
			mode = "Unknow";
			break;
		}
		return mode;
	}
}
