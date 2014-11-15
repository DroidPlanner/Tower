package com.o3dr.services.android.lib.drone.connection;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Created by fhuya on 11/14/14.
 */
public class DroneSharePrefs implements Parcelable {

    private final String username;
    private final String password;
    private final boolean isEnabled;
    private final boolean enableLiveUpload;

    public DroneSharePrefs(String username, String password, boolean isEnabled,
                           boolean enableLiveUpload) {
        this.username = username;
        this.password = password;
        this.isEnabled = isEnabled;
        this.enableLiveUpload = enableLiveUpload;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean isLiveUploadEnabled() {
        return enableLiveUpload;
    }

    public boolean areLoginCredentialsSet(){
        return !TextUtils.isEmpty(username) && !TextUtils.isEmpty(password);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DroneSharePrefs)) return false;

        DroneSharePrefs that = (DroneSharePrefs) o;

        if (enableLiveUpload != that.enableLiveUpload) return false;
        if (isEnabled != that.isEnabled) return false;
        if (password != null ? !password.equals(that.password) : that.password != null)
            return false;
        if (username != null ? !username.equals(that.username) : that.username != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (isEnabled ? 1 : 0);
        result = 31 * result + (enableLiveUpload ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DroneSharePrefs{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", isEnabled=" + isEnabled +
                ", enableLiveUpload=" + enableLiveUpload +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.username);
        dest.writeString(this.password);
        dest.writeByte(isEnabled ? (byte) 1 : (byte) 0);
        dest.writeByte(enableLiveUpload ? (byte) 1 : (byte) 0);
    }

    private DroneSharePrefs(Parcel in) {
        this.username = in.readString();
        this.password = in.readString();
        this.isEnabled = in.readByte() != 0;
        this.enableLiveUpload = in.readByte() != 0;
    }

    public static final Creator<DroneSharePrefs> CREATOR = new Creator<DroneSharePrefs>() {
        public DroneSharePrefs createFromParcel(Parcel source) {
            return new DroneSharePrefs(source);
        }

        public DroneSharePrefs[] newArray(int size) {
            return new DroneSharePrefs[size];
        }
    };
}
