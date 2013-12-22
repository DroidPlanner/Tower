/** 
*/
package com.MAVLink.Messages.enums;

public class LIMITS_STATE {
	public static final int LIMITS_INIT = 0; /*  pre-initialization | */
	public static final int LIMITS_DISABLED = 1; /*  disabled | */
	public static final int LIMITS_ENABLED = 2; /*  checking limits | */
	public static final int LIMITS_TRIGGERED = 3; /*  a limit has been breached | */
	public static final int LIMITS_RECOVERING = 4; /*  taking action eg. RTL | */
	public static final int LIMITS_RECOVERED = 5; /*  we're no longer in breach of a limit | */
	public static final int LIMITS_STATE_ENUM_END = 6; /*  | */
}
