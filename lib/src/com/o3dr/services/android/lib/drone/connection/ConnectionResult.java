package com.o3dr.services.android.lib.drone.connection;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Conveys information if the connection attempt fails.
 */
public final class ConnectionResult implements Parcelable {

    private final int mErrorCode;
    private final String mErrorMessage;

    public ConnectionResult(int errorCode, String errorMessage) {
        this.mErrorCode = errorCode;
        this.mErrorMessage = errorMessage;
    }

    public int getErrorCode() {
        return mErrorCode;
    }

    public String getErrorMessage() {
        return mErrorMessage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mErrorCode);
        dest.writeString(this.mErrorMessage);
    }

    private ConnectionResult(Parcel in) {
        this.mErrorCode = in.readInt();
        this.mErrorMessage = in.readString();
    }

    public static final Parcelable.Creator<ConnectionResult> CREATOR = new Parcelable.Creator<ConnectionResult>() {
        public ConnectionResult createFromParcel(Parcel source) {
            return new ConnectionResult(source);
        }

        public ConnectionResult[] newArray(int size) {
            return new ConnectionResult[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConnectionResult)) return false;

        ConnectionResult that = (ConnectionResult) o;

        if (mErrorCode != that.mErrorCode) return false;
        if (mErrorMessage != null ? !mErrorMessage.equals(that.mErrorMessage) : that.mErrorMessage != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mErrorCode;
        result = 31 * result + (mErrorMessage != null ? mErrorMessage.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ConnectionResult{" +
                "mErrorCode=" + mErrorCode +
                ", mErrorMessage='" + mErrorMessage + '\'' +
                '}';
    }
}
