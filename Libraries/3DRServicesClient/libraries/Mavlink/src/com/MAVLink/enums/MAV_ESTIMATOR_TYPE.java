            /** Enumeration of estimator types
            */
            package com.MAVLink.enums;
            
            public class MAV_ESTIMATOR_TYPE {
            	public static final int MAV_ESTIMATOR_TYPE_NAIVE = 1; /* This is a naive estimator without any real covariance feedback. | */
            	public static final int MAV_ESTIMATOR_TYPE_VISION = 2; /* Computer vision based estimate. Might be up to scale. | */
            	public static final int MAV_ESTIMATOR_TYPE_VIO = 3; /* Visual-inertial estimate. | */
            	public static final int MAV_ESTIMATOR_TYPE_GPS = 4; /* Plain GPS estimate. | */
            	public static final int MAV_ESTIMATOR_TYPE_GPS_INS = 5; /* Estimator integrating GPS and inertial sensing. | */
            	public static final int MAV_ESTIMATOR_TYPE_ENUM_END = 6; /*  | */
            
            }
            