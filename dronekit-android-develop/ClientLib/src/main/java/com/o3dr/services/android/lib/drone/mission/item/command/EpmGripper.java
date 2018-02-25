package com.o3dr.services.android.lib.drone.mission.item.command;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;

/**
 * Created by fhuya on 11/6/14.
 */
public class EpmGripper extends MissionItem implements MissionItem.Command, android.os.Parcelable {

    private boolean release;

    public EpmGripper(){
        super(MissionItemType.EPM_GRIPPER);
    }

    public EpmGripper(EpmGripper copy){
        this();
        release = copy.release;
    }

    public boolean isRelease() {
        return release;
    }

    public void setRelease(boolean release) {
        this.release = release;
    }

    @Override
    public String toString() {
        return "EpmGripper{" +
                "release=" + release +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EpmGripper)) return false;
        if (!super.equals(o)) return false;

        EpmGripper that = (EpmGripper) o;

        return release == that.release;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (release ? 1 : 0);
        return result;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte(release ? (byte) 1 : (byte) 0);
    }

    private EpmGripper(Parcel in) {
        super(in);
        this.release = in.readByte() != 0;
    }

    @Override
    public MissionItem clone() {
        return new EpmGripper(this);
    }

    public static final Creator<EpmGripper> CREATOR = new Creator<EpmGripper>() {
        public EpmGripper createFromParcel(Parcel source) {
            return new EpmGripper(source);
        }

        public EpmGripper[] newArray(int size) {
            return new EpmGripper[size];
        }
    };
}
