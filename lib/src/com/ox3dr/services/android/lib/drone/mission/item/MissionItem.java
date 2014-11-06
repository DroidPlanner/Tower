package com.ox3dr.services.android.lib.drone.mission.item;

import android.os.Parcel;
import android.os.Parcelable;

import com.ox3dr.services.android.lib.coordinate.LatLongAlt;

/**
 * Created by fhuya on 11/5/14.
 */
public class MissionItem implements Parcelable {

    public interface Command {}

    public interface SpatialItem {
        LatLongAlt getCoordinate();
    }

    private final int type;

    private MissionItem(){
        this.type = MissionItemType.INVALID_TYPE;
    }

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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
    }

    protected MissionItem(Parcel in) {
        this.type = in.readInt();
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
