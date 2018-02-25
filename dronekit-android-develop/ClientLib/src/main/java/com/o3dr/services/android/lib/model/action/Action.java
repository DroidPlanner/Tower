package com.o3dr.services.android.lib.model.action;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Wrapper for action exposed by the api.
 */
public class Action implements Parcelable {

    private String type;
    private Bundle data;

    public Action(String actionType){
        this.type = actionType;
        this.data = null;
    }

    public Action(String actionType, Bundle actionData){
        this.type = actionType;
        this.data = actionData;
    }

    public String getType() {
        return type;
    }

    public Bundle getData() {
        return data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.type);
        dest.writeBundle(data);
    }

    public void readFromParcel(Parcel source){
        this.type = source.readString();
        data = source.readBundle();
    }

    private Action(Parcel in) {
        readFromParcel(in);
    }

    public static final Parcelable.Creator<Action> CREATOR = new Parcelable.Creator<Action>() {
        public Action createFromParcel(Parcel source) {
            return new Action(source);
        }

        public Action[] newArray(int size) {
            return new Action[size];
        }
    };
}
