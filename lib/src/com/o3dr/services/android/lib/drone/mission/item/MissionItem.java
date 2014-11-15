package com.o3dr.services.android.lib.drone.mission.item;

import android.os.Parcel;
import android.os.Parcelable;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;

import java.io.Serializable;

/**
 * Created by fhuya on 11/5/14.
 */
public abstract class MissionItem implements Parcelable, Serializable {

    public interface Command {}

    public interface SpatialItem {
        LatLongAlt getCoordinate();

        void setCoordinate(LatLongAlt coordinate);
    }

    public interface ComplexItem {
    }

    private final MissionItemType type;

    protected MissionItem(MissionItemType type) {
        this.type = type;
    }

    public MissionItemType getType() {
        return type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this);
    }

    public static final Creator<MissionItem> CREATOR = new Creator<MissionItem>() {
        public MissionItem createFromParcel(Parcel source) {
            return (MissionItem) source.readSerializable();
        }

        public MissionItem[] newArray(int size) {
            return new MissionItem[size];
        }
    };
}
