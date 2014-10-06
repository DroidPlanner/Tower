package com.three_dr.services.android.lib.drone;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Base type used to push updates to a drone instance.
 */
public abstract class DroneUpdateRequest implements Parcelable, Serializable {

    @Override
    public final int describeContents(){
        return 0;
    }

    @Override
    public final void writeToParcel(Parcel dest, int flags) {
        //Serialize 'this' so we can get it back after ipc
        dest.writeSerializable(this);
    }

    public static final Creator<DroneUpdateRequest> CREATOR = new Creator<DroneUpdateRequest>(){

        /**
         * Read the drone update request from the parcel.
         *
         * {@inheritDoc}
         *
         * @param source
         * @return a concrete drone update request.
         */
        @Override
        public final DroneUpdateRequest createFromParcel(Parcel source) {
            return (DroneUpdateRequest) source.readSerializable();
        }

        @Override
        public final DroneUpdateRequest[] newArray(int size) {
            return new DroneUpdateRequest[size];
        }
    };

}
