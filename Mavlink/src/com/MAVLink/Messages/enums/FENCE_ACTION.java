/** 
*/
package com.MAVLink.Messages.enums;

public class FENCE_ACTION {
	public static final int FENCE_ACTION_NONE = 0; /* Disable fenced mode | */
	public static final int FENCE_ACTION_GUIDED = 1; /* Switched to guided mode to return point (fence point 0) | */
	public static final int FENCE_ACTION_REPORT = 2; /* Report fence breach, but don't take action | */
	public static final int FENCE_ACTION_ENUM_END = 3; /*  | */
}
