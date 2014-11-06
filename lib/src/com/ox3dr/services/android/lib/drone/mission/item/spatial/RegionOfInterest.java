package com.ox3dr.services.android.lib.drone.mission.item.spatial;

import android.os.Parcel;

import com.ox3dr.services.android.lib.drone.mission.item.MissionItemType;

/**
 * Created by fhuya on 11/6/14.
 */
public class RegionOfInterest extends BaseSpatialItem {

    public RegionOfInterest(){
        super(MissionItemType.REGION_OF_INTEREST);
    }

    public static final Creator<RegionOfInterest> CREATOR = new Creator<RegionOfInterest>() {
        @Override
        public RegionOfInterest createFromParcel(Parcel source) {
            return (RegionOfInterest) source.readSerializable();
        }

        @Override
        public RegionOfInterest[] newArray(int size) {
            return new RegionOfInterest[size];
        }
    };
}
