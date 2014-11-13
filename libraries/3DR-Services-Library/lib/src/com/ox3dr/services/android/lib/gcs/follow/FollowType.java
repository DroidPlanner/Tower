package com.ox3dr.services.android.lib.gcs.follow;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fhuya on 11/5/14.
 */
public enum FollowType implements Parcelable {

    LEASH("Leash"),
    LEAD("Lead"),
    RIGHT("Right"),
    LEFT("Left"),
    CIRCLE("Circle"),
    ABOVE("Above");

    private final String typeLabel;

    private FollowType(String typeLabel) {
        this.typeLabel = typeLabel;
    }

    public String getTypeLabel() {
        return typeLabel;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name());
    }

    public static final Parcelable.Creator<FollowType> CREATOR = new Parcelable.Creator<FollowType>() {
        public FollowType createFromParcel(Parcel source) {
            return FollowType.valueOf(source.readString());
        }

        public FollowType[] newArray(int size) {
            return new FollowType[size];
        }
    };
}
