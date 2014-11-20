            /** 
            */
            package com.MAVLink.enums;
            
            public class MAV_FRAME {
            	public static final int MAV_FRAME_GLOBAL = 0; /* Global coordinate frame, WGS84 coordinate system. First value / x: latitude, second value / y: longitude, third value / z: positive altitude over mean sea level (MSL) | */
            	public static final int MAV_FRAME_LOCAL_NED = 1; /* Local coordinate frame, Z-up (x: north, y: east, z: down). | */
            	public static final int MAV_FRAME_MISSION = 2; /* NOT a coordinate frame, indicates a mission command. | */
            	public static final int MAV_FRAME_GLOBAL_RELATIVE_ALT = 3; /* Global coordinate frame, WGS84 coordinate system, relative altitude over ground with respect to the home position. First value / x: latitude, second value / y: longitude, third value / z: positive altitude with 0 being at the altitude of the home location. | */
            	public static final int MAV_FRAME_LOCAL_ENU = 4; /* Local coordinate frame, Z-down (x: east, y: north, z: up) | */
            	public static final int MAV_FRAME_LOCAL_OFFSET_NED = 7; /* Offset to the current local frame. Anything expressed in this frame should be added to the current local frame position. | */
            	public static final int MAV_FRAME_BODY_NED = 8; /* Setpoint in body NED frame. This makes sense if all position control is externalized - e.g. useful to command 2 m/s^2 acceleration to the right. | */
            	public static final int MAV_FRAME_BODY_OFFSET_NED = 9; /* Offset in body NED frame. This makes sense if adding setpoints to the current flight path, to avoid an obstacle - e.g. useful to command 2 m/s^2 acceleration to the east. | */
            	public static final int MAV_FRAME_GLOBAL_TERRAIN_ALT = 10; /* Global coordinate frame with above terrain level altitude. WGS84 coordinate system, relative altitude over terrain with respect to the waypoint coordinate. First value / x: latitude in degrees, second value / y: longitude in degrees, third value / z: positive altitude in meters with 0 being at ground level in terrain model. | */
            	public static final int MAV_FRAME_ENUM_END = 11; /*  | */
            
            }
            