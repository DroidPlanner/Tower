package com.three_dr.services.android.lib.drone.connection;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Base type used to pass the drone connection parameters over ipc.
 */
public abstract class ConnectionParameter implements Parcelable, Serializable {
    @Override
    public final int describeContents() {
        return 0;
    }

    @Override
    public final void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this);
    }

    public static final Creator<ConnectionParameter> CREATOR = new Creator<ConnectionParameter>(){

        @Override
        public final ConnectionParameter createFromParcel(Parcel source) {
            return (ConnectionParameter) source.readSerializable();
        }

        @Override
        public final ConnectionParameter[] newArray(int size) {
            return new ConnectionParameter[size];
        }
    };

}
