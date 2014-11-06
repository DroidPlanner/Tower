package com.ox3dr.services.android.lib.drone.mission.item;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fhuya on 11/5/14.
 */
public class MissionItem implements Parcelable {

    private int type;

    protected MissionItem(){}

    protected MissionItem(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    protected void readFromParcel(Parcel in){
        this.type = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
    }

    private MissionItem(Parcel in) {
        readFromParcel(in);
    }

    public static final Creator<MissionItem> CREATOR = new Creator<MissionItem>() {
        public MissionItem createFromParcel(Parcel source) {
            return new MissionItem(source);
        }

        public MissionItem[] newArray(int size) {
            return new MissionItem[size];
        }
    };
}
