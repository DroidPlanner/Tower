package com.three_dr.services.android.lib.drone.property;

/**
 * Created by fhuya on 10/28/14.
 */
public class State  {

    private boolean armed;
    private boolean isFlying;
    private VehicleMode vehicleMode;
    private Type vehicleType;

    public State(VehicleMode mode, Type type, boolean armed, boolean flying){
        this.vehicleMode = mode;
        this.vehicleType = type;
        this.armed = armed;
        this.isFlying = flying;
    }

    public boolean isArmed() {
        return armed;
    }

    public boolean isFlying() {
        return isFlying;
    }

    public VehicleMode getVehicleMode() {
        return vehicleMode;
    }

    public Type getVehicleType(){
        return vehicleType;
    }
}
