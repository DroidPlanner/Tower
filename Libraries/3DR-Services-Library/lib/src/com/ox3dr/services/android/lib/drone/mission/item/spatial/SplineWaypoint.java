package com.ox3dr.services.android.lib.drone.mission.item.spatial;

import android.os.Parcel;

import com.ox3dr.services.android.lib.drone.mission.item.MissionItemType;

/**
 * Created by fhuya on 11/6/14.
 */
public class SplineWaypoint extends BaseSpatialItem {

    /**
     * Hold time in decimal seconds. (ignored by fixed wing, time to stay at
     * MISSION for rotary wing)
     */
    private double delay;

    public SplineWaypoint(){
        super(MissionItemType.SPLINE_WAYPOINT, "Spline Waypoint");
    }

    public double getDelay() {
        return delay;
    }

    public void setDelay(double delay) {
        this.delay = delay;
    }

    public static final Creator<SplineWaypoint> CREATOR = new Creator<SplineWaypoint>() {
        @Override
        public SplineWaypoint createFromParcel(Parcel source) {
            return (SplineWaypoint) source.readSerializable();
        }

        @Override
        public SplineWaypoint[] newArray(int size) {
            return new SplineWaypoint[size];
        }
    };
}
