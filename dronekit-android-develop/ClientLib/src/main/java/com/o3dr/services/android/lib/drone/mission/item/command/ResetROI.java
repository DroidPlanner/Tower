package com.o3dr.services.android.lib.drone.mission.item.command;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;

/**
 * Reset the current region of interest lock.
 * Created by Fredia Huya-Kouadio on 10/20/15.
 * @since 2.6.8
 */
public class ResetROI extends MissionItem implements MissionItem.Command {

    public ResetROI(){
        super(MissionItemType.RESET_ROI);
    }

    public ResetROI(ResetROI copy){
        this();
    }

    @Override
    public MissionItem clone() {
        return new ResetROI(this);
    }

    @Override
    public String toString() {
        return "ResetROI{}";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    protected ResetROI(Parcel in) {
        super(in);
    }

    public static final Creator<ResetROI> CREATOR = new Creator<ResetROI>() {
        public ResetROI createFromParcel(Parcel source) {
            return new ResetROI(source);
        }

        public ResetROI[] newArray(int size) {
            return new ResetROI[size];
        }
    };
}
