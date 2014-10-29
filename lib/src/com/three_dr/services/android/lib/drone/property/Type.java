package com.three_dr.services.android.lib.drone.property;

/**
 * Stores information about the drone's type.
 */
public class Type {

    public static final int TYPE_PLANE = 1;
    public static final int TYPE_COPTER = 2;
    public static final int TYPE_ROVER = 10;

    private final int droneType;

    public Type(int droneType){
        if(droneType != TYPE_PLANE && droneType != TYPE_COPTER && droneType != TYPE_ROVER){
            throw new IllegalArgumentException("Unexpected drone type parameter (" + droneType +
                    ")");
        }

        this.droneType = droneType;
    }

    public int getDroneType(){
        return droneType;
    }
}
