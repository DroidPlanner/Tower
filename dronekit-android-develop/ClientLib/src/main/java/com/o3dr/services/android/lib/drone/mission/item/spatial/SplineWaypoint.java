package com.o3dr.services.android.lib.drone.mission.item.spatial;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;

/**
 * Created by fhuya on 11/6/14.
 */
public class SplineWaypoint extends BaseSpatialItem implements android.os.Parcelable {

    /**
     * Hold time in decimal seconds. (ignored by fixed wing, time to stay at
     * MISSION for rotary wing)
     */
    private double delay;

    public SplineWaypoint(){
        super(MissionItemType.SPLINE_WAYPOINT);
    }

    public SplineWaypoint(SplineWaypoint copy){
        super(copy);
        this.delay = copy.delay;
    }

    public double getDelay() {
        return delay;
    }

    public void setDelay(double delay) {
        this.delay = delay;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(this.delay);
    }

    private SplineWaypoint(Parcel in) {
        super(in);
        this.delay = in.readDouble();
    }

    @Override
    public String toString() {
        return "SplineWaypoint{" +
                "delay=" + delay +
                ", " + super.toString() +
        '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SplineWaypoint)) return false;
        if (!super.equals(o)) return false;

        SplineWaypoint that = (SplineWaypoint) o;

        return Double.compare(that.delay, delay) == 0;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(delay);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public MissionItem clone() {
        return new SplineWaypoint(this);
    }

    public static final Creator<SplineWaypoint> CREATOR = new Creator<SplineWaypoint>() {
        public SplineWaypoint createFromParcel(Parcel source) {
            return new SplineWaypoint(source);
        }

        public SplineWaypoint[] newArray(int size) {
            return new SplineWaypoint[size];
        }
    };
}
