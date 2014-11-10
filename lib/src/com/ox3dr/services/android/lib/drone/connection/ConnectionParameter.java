package com.ox3dr.services.android.lib.drone.connection;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Base type used to pass the drone connection parameters over ipc.
 */
public class ConnectionParameter implements Parcelable {

    private final int connectionType;
    private final Bundle paramsBundle;

    public ConnectionParameter(int connectionType, Bundle paramsBundle){
        this.connectionType = connectionType;
        this.paramsBundle = paramsBundle;
    }

    public int getConnectionType() {
        return connectionType;
    }

    public Bundle getParamsBundle() {
        return paramsBundle;
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(!(o instanceof ConnectionParameter)) return false;

        ConnectionParameter that = (ConnectionParameter) o;
        return toString().equals(that.toString());
    }

    @Override
    public int hashCode(){
        return toString().hashCode();
    }

    @Override
    public String toString() {
        String toString = "ConnectionParameter{" +
                "connectionType=" + connectionType +
                ", paramsBundle=[";

        if(paramsBundle != null && !paramsBundle.isEmpty()) {
            boolean isFirst = true;
            for(String key: paramsBundle.keySet()){
                if(isFirst)
                    isFirst = false;
                else
                    toString += ", ";

                toString += key + "=" + paramsBundle.get(key);
            }
        }

        toString += "]}";
        return toString;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.connectionType);
        dest.writeBundle(paramsBundle);
    }

    private ConnectionParameter(Parcel in) {
        this.connectionType = in.readInt();
        paramsBundle = in.readBundle();
    }

    public static final Creator<ConnectionParameter> CREATOR = new Creator<ConnectionParameter>() {
        public ConnectionParameter createFromParcel(Parcel source) {
            return new ConnectionParameter(source);
        }

        public ConnectionParameter[] newArray(int size) {
            return new ConnectionParameter[size];
        }
    };
}
