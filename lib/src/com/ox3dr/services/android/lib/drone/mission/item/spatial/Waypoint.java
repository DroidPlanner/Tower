package com.ox3dr.services.android.lib.drone.mission.item.spatial;

import android.os.Parcel;

import com.ox3dr.services.android.lib.drone.mission.item.MissionItemType;

/**
 * Created by fhuya on 11/6/14.
 */
public class Waypoint extends BaseSpatialItem {

    private double delay;
    private double acceptanceRadius;
    private double yawAngle;
    private double orbitalRadius;
    private boolean orbitCCW;

    public Waypoint(){
        super(MissionItemType.WAYPOINT);
    }

    public double getDelay() {
        return delay;
    }

    public void setDelay(double delay) {
        this.delay = delay;
    }

    public double getAcceptanceRadius() {
        return acceptanceRadius;
    }

    public void setAcceptanceRadius(double acceptanceRadius) {
        this.acceptanceRadius = acceptanceRadius;
    }

    public double getYawAngle() {
        return yawAngle;
    }

    public void setYawAngle(double yawAngle) {
        this.yawAngle = yawAngle;
    }

    public double getOrbitalRadius() {
        return orbitalRadius;
    }

    public void setOrbitalRadius(double orbitalRadius) {
        this.orbitalRadius = orbitalRadius;
    }

    public boolean isOrbitCCW() {
        return orbitCCW;
    }

    public void setOrbitCCW(boolean orbitCCW) {
        this.orbitCCW = orbitCCW;
    }

    public static final Creator<Waypoint> CREATOR = new Creator<Waypoint>() {
        @Override
        public Waypoint createFromParcel(Parcel source) {
            return (Waypoint) source.readSerializable();
        }

        @Override
        public Waypoint[] newArray(int size) {
            return new Waypoint[size];
        }
    };
}
