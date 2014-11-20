            /** Data stream IDs. A data stream is not a fixed set of messages, but rather a
     recommendation to the autopilot software. Individual autopilots may or may not obey
     the recommended messages.
            */
            package com.MAVLink.enums;
            
            public class MAV_DATA_STREAM {
            	public static final int MAV_DATA_STREAM_ALL = 0; /* Enable all data streams | */
            	public static final int MAV_DATA_STREAM_RAW_SENSORS = 1; /* Enable IMU_RAW, GPS_RAW, GPS_STATUS packets. | */
            	public static final int MAV_DATA_STREAM_EXTENDED_STATUS = 2; /* Enable GPS_STATUS, CONTROL_STATUS, AUX_STATUS | */
            	public static final int MAV_DATA_STREAM_RC_CHANNELS = 3; /* Enable RC_CHANNELS_SCALED, RC_CHANNELS_RAW, SERVO_OUTPUT_RAW | */
            	public static final int MAV_DATA_STREAM_RAW_CONTROLLER = 4; /* Enable ATTITUDE_CONTROLLER_OUTPUT, POSITION_CONTROLLER_OUTPUT, NAV_CONTROLLER_OUTPUT. | */
            	public static final int MAV_DATA_STREAM_POSITION = 6; /* Enable LOCAL_POSITION, GLOBAL_POSITION/GLOBAL_POSITION_INT messages. | */
            	public static final int MAV_DATA_STREAM_EXTRA1 = 10; /* Dependent on the autopilot | */
            	public static final int MAV_DATA_STREAM_EXTRA2 = 11; /* Dependent on the autopilot | */
            	public static final int MAV_DATA_STREAM_EXTRA3 = 12; /* Dependent on the autopilot | */
            	public static final int MAV_DATA_STREAM_ENUM_END = 13; /*  | */
            
            }
            