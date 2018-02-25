package com.o3dr.services.android.lib.drone.mission.item.spatial;

import android.os.Parcel;

import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;

public class DoLandStart extends BaseSpatialItem implements android.os.Parcelable {

    public DoLandStart(){
        super(MissionItemType.DO_LAND_START);
    }

    public DoLandStart(DoLandStart copy){
        super(copy);
    }

    private DoLandStart(Parcel in) {
        super(in);
    }

    @Override
    public String toString() {
        return "DoLandStart{ " + super.toString() + " }";
    }

    @Override
    public MissionItem clone() {
        return new DoLandStart(this);
    }

    public static final Creator<DoLandStart> CREATOR = new Creator<DoLandStart>() {
        public DoLandStart createFromParcel(Parcel source) {
            return new DoLandStart(source);
        }

        public DoLandStart[] newArray(int size) {
            return new DoLandStart[size];
        }
    };
}