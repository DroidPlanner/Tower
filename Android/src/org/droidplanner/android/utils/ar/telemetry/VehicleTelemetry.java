package org.droidplanner.android.utils.ar.telemetry;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;

/**
 * Model for vehicle telemetry for calculating AR camera model-view projections.
 */
public class VehicleTelemetry {
    private double gimbalYaw;
    private double gimbalPitch;
    private LatLongAlt vehicleLocation;
    private LatLongAlt homeLocation;
    private long timeStamp;

    public VehicleTelemetry(float gimbalYaw, float gimbalPitch, LatLongAlt vehicleLocation,
                            LatLongAlt homeLocation, long timeStamp) {
        this.gimbalYaw = gimbalYaw;
        this.gimbalPitch = gimbalPitch;
        this.vehicleLocation = vehicleLocation;
        this.homeLocation = homeLocation;
        this.timeStamp = timeStamp;
    }

    public double getGimbalYaw() {
        return gimbalYaw;
    }

    public void setGimbalYaw(double gimbalYaw) {
        this.gimbalYaw = gimbalYaw;
    }

    public double getGimbalPitch() {
        return gimbalPitch;
    }

    public void setGimbalPitch(double gimbalPitch) {
        this.gimbalPitch = gimbalPitch;
    }

    public LatLongAlt getVehicleLocation() {
        return vehicleLocation;
    }

    public void setVehicleLocation(LatLongAlt vehicleLocation) {
        this.vehicleLocation = vehicleLocation;
    }

    public LatLongAlt getHomeLocation() {
        return homeLocation;
    }

    public void setHomeLocation(LatLongAlt homeLocation) {
        this.homeLocation = homeLocation;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return "Gimbal Yaw in degrees: " + gimbalYaw + "\n" +
            "Gimbal Pitch in degrees: " + gimbalPitch + "\n" +
            "Vehicle location: " + vehicleLocation + "\n" +
            "Home location: " + vehicleLocation + "\n" +
            "Timestamp in ticks: " + timeStamp;
    }
}
