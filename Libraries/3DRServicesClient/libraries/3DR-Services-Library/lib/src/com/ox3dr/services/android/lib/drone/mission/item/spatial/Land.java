package com.ox3dr.services.android.lib.drone.mission.item.spatial;

import android.os.Parcel;

import com.ox3dr.services.android.lib.drone.mission.item.MissionItemType;

/**
 * Created by fhuya on 11/6/14.
 */
public class Land extends BaseSpatialItem {

    public Land(){
        super(MissionItemType.LAND);
    }

    public static final Creator<Land> CREATOR = new Creator<Land>() {
        @Override
        public Land createFromParcel(Parcel source) {
            return (Land) source.readSerializable();
        }

        @Override
        public Land[] newArray(int size) {
            return new Land[size];
        }
    };
}
