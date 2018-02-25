package com.o3dr.services.android.lib.drone.mission.item.command;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;

/**
 * Created by Toby on 7/31/2015.
 */
public class DoJump extends MissionItem implements MissionItem.Command, android.os.Parcelable {
    private int waypoint;
    private int repeatCount;

    public DoJump(){
        super(MissionItemType.DO_JUMP);
    }

    public DoJump(DoJump copy){
        this();
        this.waypoint = copy.waypoint;
        this.repeatCount = copy.repeatCount;
    }

    protected DoJump(Parcel in) {
        super(in);
        waypoint = in.readInt();
        repeatCount = in.readInt();
    }

    @Override
    public String toString() {
        return "DoJump{" +
                "repeatCount=" + repeatCount +
                ", waypoint=" + waypoint +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DoJump)) return false;
        if (!super.equals(o)) return false;

        DoJump doJump = (DoJump) o;

        if (waypoint != doJump.waypoint) return false;
        return repeatCount == doJump.repeatCount;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + waypoint;
        result = 31 * result + repeatCount;
        return result;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(waypoint);
        dest.writeInt(repeatCount);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getWaypoint() {
        return waypoint;
    }

    public void setWaypoint(int waypoint) {
        this.waypoint = waypoint;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public static final Creator<DoJump> CREATOR = new Creator<DoJump>() {
        @Override
        public DoJump createFromParcel(Parcel in) {
            return new DoJump(in);
        }

        @Override
        public DoJump[] newArray(int size) {
            return new DoJump[size];
        }
    };

    @Override
    public MissionItem clone() {
        return new DoJump(this);
    }
}
