package com.ox3dr.services.android.lib.gcs.follow;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fhuya on 11/5/14.
 */
public class FollowType implements Parcelable {

    public static final int TYPE_LEASH = 0;
    public static final int TYPE_LEAD = 1;
    public static final int TYPE_RIGHT = 2;
    public static final int TYPE_LEFT = 3;
    public static final int TYPE_CIRCLE = 4;
    public static final int TYPE_ABOVE = 5;

    private final int followType;
    private final String typeLabel;

    public FollowType(int followType, String typeLabel) {
        this.followType = followType;
        this.typeLabel = typeLabel;
    }

    public int getFollowType() {
        return followType;
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
        dest.writeInt(this.followType);
        dest.writeString(this.typeLabel);
    }

    private FollowType(Parcel in) {
        this.followType = in.readInt();
        this.typeLabel = in.readString();
    }

    public static final Parcelable.Creator<FollowType> CREATOR = new Parcelable.Creator<FollowType>() {
        public FollowType createFromParcel(Parcel source) {
            return new FollowType(source);
        }

        public FollowType[] newArray(int size) {
            return new FollowType[size];
        }
    };
}
