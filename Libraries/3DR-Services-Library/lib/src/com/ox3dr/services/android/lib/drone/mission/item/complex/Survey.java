package com.ox3dr.services.android.lib.drone.mission.item.complex;

import android.os.Parcel;

import com.ox3dr.services.android.lib.drone.mission.item.MissionItem;
import com.ox3dr.services.android.lib.drone.mission.item.MissionItemType;

/**
 * TODO: not yet complete.
 */
public class Survey extends MissionItem {

    public Survey(){
        super(MissionItemType.SURVEY);
    }

    public static final Creator<Survey> CREATOR = new Creator<Survey>() {
        @Override
        public Survey createFromParcel(Parcel source) {
            return (Survey) source.readSerializable();
        }

        @Override
        public Survey[] newArray(int size) {
            return new Survey[size];
        }
    };
}
