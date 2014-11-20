            /** These encode the sensors whose status is sent as part of the SYS_STATUS message.
            */
            package com.MAVLink.enums;
            
            public class MAV_SYS_STATUS_SENSOR {
            	public static final int MAV_SYS_STATUS_SENSOR_3D_GYRO = 1; /* 0x01 3D gyro | */
            	public static final int MAV_SYS_STATUS_SENSOR_3D_ACCEL = 2; /* 0x02 3D accelerometer | */
            	public static final int MAV_SYS_STATUS_SENSOR_3D_MAG = 4; /* 0x04 3D magnetometer | */
            	public static final int MAV_SYS_STATUS_SENSOR_ABSOLUTE_PRESSURE = 8; /* 0x08 absolute pressure | */
            	public static final int MAV_SYS_STATUS_SENSOR_DIFFERENTIAL_PRESSURE = 16; /* 0x10 differential pressure | */
            	public static final int MAV_SYS_STATUS_SENSOR_GPS = 32; /* 0x20 GPS | */
            	public static final int MAV_SYS_STATUS_SENSOR_OPTICAL_FLOW = 64; /* 0x40 optical flow | */
            	public static final int MAV_SYS_STATUS_SENSOR_VISION_POSITION = 128; /* 0x80 computer vision position | */
            	public static final int MAV_SYS_STATUS_SENSOR_LASER_POSITION = 256; /* 0x100 laser based position | */
            	public static final int MAV_SYS_STATUS_SENSOR_EXTERNAL_GROUND_TRUTH = 512; /* 0x200 external ground truth (Vicon or Leica) | */
            	public static final int MAV_SYS_STATUS_SENSOR_ANGULAR_RATE_CONTROL = 1024; /* 0x400 3D angular rate control | */
            	public static final int MAV_SYS_STATUS_SENSOR_ATTITUDE_STABILIZATION = 2048; /* 0x800 attitude stabilization | */
            	public static final int MAV_SYS_STATUS_SENSOR_YAW_POSITION = 4096; /* 0x1000 yaw position | */
            	public static final int MAV_SYS_STATUS_SENSOR_Z_ALTITUDE_CONTROL = 8192; /* 0x2000 z/altitude control | */
            	public static final int MAV_SYS_STATUS_SENSOR_XY_POSITION_CONTROL = 16384; /* 0x4000 x/y position control | */
            	public static final int MAV_SYS_STATUS_SENSOR_MOTOR_OUTPUTS = 32768; /* 0x8000 motor outputs / control | */
            	public static final int MAV_SYS_STATUS_SENSOR_RC_RECEIVER = 65536; /* 0x10000 rc receiver | */
            	public static final int MAV_SYS_STATUS_SENSOR_3D_GYRO2 = 131072; /* 0x20000 2nd 3D gyro | */
            	public static final int MAV_SYS_STATUS_SENSOR_3D_ACCEL2 = 262144; /* 0x40000 2nd 3D accelerometer | */
            	public static final int MAV_SYS_STATUS_SENSOR_3D_MAG2 = 524288; /* 0x80000 2nd 3D magnetometer | */
            	public static final int MAV_SYS_STATUS_GEOFENCE = 1048576; /* 0x100000 geofence | */
            	public static final int MAV_SYS_STATUS_AHRS = 2097152; /* 0x200000 AHRS subsystem health | */
            	public static final int MAV_SYS_STATUS_TERRAIN = 4194304; /* 0x400000 Terrain subsystem health | */
            	public static final int MAV_SYS_STATUS_SENSOR_ENUM_END = 4194305; /*  | */
            
            }
            